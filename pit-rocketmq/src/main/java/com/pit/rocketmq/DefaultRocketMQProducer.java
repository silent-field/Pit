package com.pit.rocketmq;

import com.google.common.base.Strings;
import com.pit.core.json.GsonUtils;
import com.pit.rocketmq.config.ProducerConfig;
import com.pit.rocketmq.config.RocketMQProperties;
import com.pit.rocketmq.entity.JsonMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/23
 */
@Slf4j
public class DefaultRocketMQProducer implements Closeable, ApplicationListener<ContextClosedEvent> {
    @Autowired
    private RocketMQProperties rocketMQProperties;
    private ProducerConfig producerConfig;

    private final int SEND_MSG_TIMEOUT = 3000;
    private String env;
    private String envSuffix;
    private String groupName;
    private String defaultTopic;

    private DefaultMQProducer producer;

    public DefaultRocketMQProducer(String env, String groupName, String defaultTopic) {
        this.env = env;
        this.envSuffix = StringUtils.isBlank(env) ? "" : "_" + env;
        this.groupName = groupName;
        this.defaultTopic = defaultTopic;
    }

    public DefaultRocketMQProducer(String groupName, String topic) {
        this("", groupName, topic);
    }

    @PostConstruct
    private void initProducer() {
        try {
            producerConfig = rocketMQProperties.getProducerConfig(defaultTopic);
            if (producerConfig != null) {
                log.info("RocketMQ producer topic:{} nameServer:{} producerConfig:{}", defaultTopic + envSuffix, rocketMQProperties.getNameServer(), producerConfig);
                return;
            }
            producer = new DefaultMQProducer(groupName + envSuffix);
            producer.setInstanceName(System.currentTimeMillis() + "");
            producer.setNamesrvAddr(rocketMQProperties.getNameServer());

            if (producerConfig.getSendMsgTimeout() != null) {
                producer.setSendMsgTimeout(producerConfig.getSendMsgTimeout());
            }
            if (producerConfig.getRetryTimesWhenSendFailed() != null) {
                producer.setRetryTimesWhenSendFailed(producerConfig.getRetryTimesWhenSendFailed());
            }
            producer.start();
            log.info("initProducer_success, topic:{} nameServer:{} producerConfig:{}", defaultTopic + envSuffix, rocketMQProperties.getNameServer(), producerConfig);
        } catch (MQClientException e) {
            log.error("initProducer_fail! groupName:{},topic:{},nameServer:{}", groupName + envSuffix, defaultTopic + envSuffix, rocketMQProperties.getNameServer(), e);
        }
    }

    /**
     * 发送消息(JSON格式)
     *
     * @param tag
     * @param msg
     * @return
     */
    public boolean sendMsg(String tag, JsonMsg msg) {
        return sendMsg(this.defaultTopic, tag, msg);
    }

    /**
     * 发送消息(字符串)
     *
     * @param tag
     * @param data
     * @return
     */
    public boolean sendMsg(String tag, String data) {
        return sendMsg(this.defaultTopic, tag, data);
    }

    /**
     * @param topic
     * @param tag
     * @param jsonMsg
     * @return
     */
    public boolean sendMsg(String topic, String tag, JsonMsg jsonMsg) {
        preHandleJsonMsg(jsonMsg);
        String data = toJSONString(jsonMsg);
        return sendMsg(topic, tag, data);
    }

    public boolean sendMsg(String topic, String tag, String data) {
        return sendMsg(topic, tag, data, null);
    }

    /**
     * 返回 true 并不意味着已经发布成功
     *
     * @param topic
     * @param tag
     * @param data
     * @return
     */
    public boolean sendMsgAsync(String topic, String tag, String data) {
        return sendMsgAsync(topic, tag, data, null);
    }

    /**
     * 返回 true 并不意味着已经发布成功
     *
     * @param topic
     * @param tag
     * @param data
     * @return
     */
    public boolean sendMsgOneway(String topic, String tag, String data) {
        return sendMsgOneway(topic, tag, data, null);
    }

    public boolean sendMsg(String topic, String tag, String data, String key) {
        if (Strings.isNullOrEmpty(topic)) {
            throw new IllegalArgumentException("topic is null or empty");
        }
        try {
            Message msg = new Message(topic + envSuffix, tag, data.getBytes(StandardCharsets.UTF_8));
            msg.setKeys(key);
            ProducerConfig config = rocketMQProperties.getProducerConfig(topic);
            int timeout = 0;
            if (config != null && config.getSendMsgTimeout() != null) {
                timeout = config.getSendMsgTimeout();
            }
            if (timeout <= 0) {
                timeout = SEND_MSG_TIMEOUT;
            }
            SendResult sendResult = producer.send(msg, timeout);
            if (sendResult == null || SendStatus.SEND_OK != sendResult.getSendStatus()) {
                log.error("sendMsg_fail, topic:{} tag:{} msg:{} result:{}", topic + envSuffix, tag, data, sendResult);
                return false;
            }
            log.info("sendMsg_success, topic:{} tag:{} msg:{}", topic + envSuffix, tag, data);
            return true;
        } catch (Exception e) {
            log.error("sendMsg_fail! groupName:{} topic:{} tag:{} msg:{} ", groupName + envSuffix, topic + envSuffix, tag, data, e);
            return false;
        }
    }

    public boolean sendMsgAsync(String topic, String tag, String data, String key) {
        if (Strings.isNullOrEmpty(topic)) {
            throw new IllegalArgumentException("topic is null or empty");
        }
        try {
            Message msg = new Message(topic + envSuffix, tag, data.getBytes(StandardCharsets.UTF_8));
            msg.setKeys(key);
            ProducerConfig config = rocketMQProperties.getProducerConfig(topic);
            int timeout = 0;
            if (config != null && config.getSendMsgTimeout() != null) {
                timeout = config.getSendMsgTimeout();
            }
            if (timeout <= 0) {
                timeout = SEND_MSG_TIMEOUT;
            }
            producer.send(msg, new DefaultSendCallback(topic + envSuffix, tag, data), timeout);
            return true;
        } catch (Exception e) {
            log.error("sendMsg_fail! groupName:{} topic:{} tag:{} msg:{} ", groupName + envSuffix, topic + envSuffix, tag, data, e);
            return false;
        }
    }

    public boolean sendMsgOneway(String topic, String tag, String data, String key) {
        if (Strings.isNullOrEmpty(topic)) {
            throw new IllegalArgumentException("topic is null or empty");
        }
        try {
            Message msg = new Message(topic + envSuffix, tag, data.getBytes(StandardCharsets.UTF_8));
            msg.setKeys(key);
            producer.sendOneway(msg);
            return true;
        } catch (Exception e) {
            log.error("sendMsg_fail! groupName:{} topic:{} tag:{} msg:{} ", groupName + envSuffix, topic + envSuffix, tag, data, e);
            return false;
        }
    }

    /**
     * 发送延迟消息
     *
     * @param tag
     * @param jsonMsg
     * @param delayLevelEnum
     * @return
     */
    protected boolean sendDelayMsg(String tag, JsonMsg jsonMsg, DelayLevelEnum delayLevelEnum) {
        preHandleJsonMsg(jsonMsg);
        String data = toJSONString(jsonMsg);
        return sendDelayMsg(this.defaultTopic, tag, data, null, delayLevelEnum);
    }

    protected boolean sendDelayMsg(String topic, String tag, String data, String key, DelayLevelEnum delayLevelEnum) {
        if (Strings.isNullOrEmpty(topic)) {
            throw new IllegalArgumentException("topic is null or empty");
        }
        try {
            Message msg = new Message(topic + envSuffix, tag, data.getBytes(StandardCharsets.UTF_8));
            msg.setDelayTimeLevel(delayLevelEnum.getLevel());
            msg.setKeys(key);
            ProducerConfig config = rocketMQProperties.getProducerConfig(topic);
            int timeout = 0;
            if (config != null && config.getSendMsgTimeout() != null) {
                timeout = config.getSendMsgTimeout();
            }
            if (timeout <= 0) {
                timeout = SEND_MSG_TIMEOUT;
            }
            SendResult sendResult = producer.send(msg, timeout);
            if (sendResult == null || SendStatus.SEND_OK != sendResult.getSendStatus()) {
                log.error("sendDelayMsg_fail, topic:{} tag:{} delay:{} msg:{} result:{}", topic + envSuffix, tag, delayLevelEnum, data, sendResult);
                return false;
            }
            log.info("sendDelayMsg_success, topic:{} tag:{} delay:{} msg:{}", topic + envSuffix, tag, delayLevelEnum, data);
            return true;
        } catch (Exception e) {
            log.error("sendDelayMsg_fail, topic:{} tag:{} delay:{} msg:{}", topic + envSuffix, tag, delayLevelEnum, data, e);
            return false;
        }
    }

    private void preHandleJsonMsg(JsonMsg jsonMsg) {
        if (StringUtils.isBlank(jsonMsg.getTraceId())) {
            jsonMsg.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        }
    }

    protected String toJSONString(JsonMsg jsonMsg) {
        return GsonUtils.toJson(jsonMsg);
    }

    @Override
    public void close() {
        if (producer != null) {
            producer.shutdown();
            log.info("shutdownProducer, topic:{} group:{}", defaultTopic + envSuffix, groupName + envSuffix);
        }
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        close();
    }

    /**
     * 默认异步发送请求callback
     */
    public static class DefaultSendCallback implements SendCallback {
        private final String topic;
        private final String tag;
        private final String data;

        public DefaultSendCallback(String topic, String tag, String data) {
            this.topic = topic;
            this.tag = tag;
            this.data = data;
        }

        @Override
        public void onSuccess(SendResult sendResult) {
            if (sendResult == null || SendStatus.SEND_OK != sendResult.getSendStatus()) {
                log.error("sendMsg fail, topic:{} tag:{} msg:{} result:{}", topic, tag, data, sendResult);
                return;
            }
            log.info("sendMsg success, topic:{} tag:{} msg:{} msgId: {}", topic, tag, data, sendResult.getMsgId());

        }

        @Override
        public void onException(Throwable e) {
            log.error("sendMsg fail! topic:{} tag:{} msg:{} ", topic, tag, data, e);
        }
    }
}
