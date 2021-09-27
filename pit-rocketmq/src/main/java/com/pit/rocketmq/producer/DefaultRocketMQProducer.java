package com.pit.rocketmq.producer;

import com.google.common.base.Strings;
import com.pit.core.json.GsonUtils;
import com.pit.rocketmq.DelayLevelEnum;
import com.pit.rocketmq.config.ProducerConfig;
import com.pit.rocketmq.entity.JsonMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/26
 */
@Slf4j
public class DefaultRocketMQProducer {
    private ProducerConfig config;

    private String topic;
    private String groupName;
    private String envSuffix;

    private DefaultMQProducer producer;

    public DefaultRocketMQProducer(String nameServer, String env, ProducerConfig config) {
        this.config = config;

        if (StringUtils.isBlank(nameServer)) {
            log.error("rocketMqProducer's nameServer is null, init failed");
            return;
        }

        if (StringUtils.isBlank(config.getTopic())) {
            log.error("rocketMqProducer's topic is null, init failed");
            return;
        }

        if (StringUtils.isBlank(config.getGroupName())) {
            log.error("rocketMqProducer's groupName is null, init failed");
            return;
        }

        envSuffix = StringUtils.isBlank(env) ? "" : "_" + env;
        groupName = config.getGroupName();
        topic = config.getTopic();

        producer = new DefaultMQProducer(groupName + envSuffix);
        producer.setInstanceName(System.currentTimeMillis() + "");
        producer.setNamesrvAddr(nameServer);


        if (config.getMaxMessageSize() != null) {
            producer.setMaxMessageSize(config.getMaxMessageSize());
        }

        if (config.getSendMsgTimeout() != null) {
            producer.setSendMsgTimeout(config.getSendMsgTimeout());
        }
        if (config.getRetryTimesWhenSendFailed() != null) {
            producer.setRetryTimesWhenSendFailed(config.getRetryTimesWhenSendFailed());
        }
    }

    public void start() throws MQClientException {
        if (producer != null) {
            producer.start();
        }
    }

    public void shutdown() {
        if (producer != null) {
            producer.shutdown();
        }
    }

    // ------------- send sync

    /**
     * 发送消息(JSON格式)
     *
     * @param tag
     * @param msg
     * @return
     */
    public boolean sendMsg(String tag, JsonMsg msg) {
        return sendMsg(topic, tag, msg);
    }

    private boolean sendMsg(String topic, String tag, JsonMsg jsonMsg) {
        preHandleJsonMsg(jsonMsg);
        String data = toJSONString(jsonMsg);
        return sendMsg(topic, tag, data);
    }

    private void preHandleJsonMsg(JsonMsg jsonMsg) {
        if (StringUtils.isBlank(jsonMsg.getTraceId())) {
            jsonMsg.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        }
    }

    protected String toJSONString(JsonMsg jsonMsg) {
        return GsonUtils.toJson(jsonMsg);
    }

    /**
     * 发送消息(字符串)
     *
     * @param tag
     * @param data
     * @return
     */
    public boolean sendMsg(String tag, String data) {
        return sendMsg(topic, tag, data);
    }


    private boolean sendMsg(String topic, String tag, String data) {
        return sendMsg(topic, tag, data, null);
    }

    private boolean sendMsg(String topic, String tag, String data, String key) {
        if (Strings.isNullOrEmpty(topic)) {
            throw new IllegalArgumentException("topic is null or empty");
        }

        try {
            Message msg = new Message(topic + envSuffix, tag, data.getBytes(StandardCharsets.UTF_8));
            msg.setKeys(key);
            int timeout = config.getSendMsgTimeout();
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
    // ------------- send sync end

    // ------------- send async

    /**
     * 异步发送，返回 true 不意味着已经发布成功
     *
     * @param tag
     * @param data
     * @return
     */
    private boolean sendMsgAsync(String tag, String data) {
        return sendMsgAsync(topic, tag, data, null);
    }

    private boolean sendMsgAsync(String topic, String tag, String data, String key) {
        if (Strings.isNullOrEmpty(topic)) {
            throw new IllegalArgumentException("topic is null or empty");
        }

        try {
            Message msg = new Message(topic + envSuffix, tag, data.getBytes(StandardCharsets.UTF_8));
            msg.setKeys(key);
            int timeout = config.getSendMsgTimeout();
            producer.send(msg, new DefaultSendCallback(topic + envSuffix, tag, data), timeout);
            return true;
        } catch (Exception e) {
            log.error("sendMsg_fail! groupName:{} topic:{} tag:{} msg:{} ", groupName + envSuffix, topic + envSuffix, tag, data, e);
            return false;
        }
    }

    // ------------- send async end

    // ------------- send oneway

    /**
     * 返回 true 并不意味着已经发布成功
     *
     * @param tag
     * @param data
     * @return
     */
    public boolean sendMsgOneway(String tag, String data) {
        return sendMsgOneway(topic, tag, data, null);
    }

    private boolean sendMsgOneway(String topic, String tag, String data, String key) {
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
    // ------------- send oneway end

    // ------------- send delay

    /**
     * 发送延迟消息
     *
     * @param tag
     * @param jsonMsg
     * @param delayLevelEnum
     * @return
     */
    public boolean sendDelayMsg(String tag, JsonMsg jsonMsg, DelayLevelEnum delayLevelEnum) {
        preHandleJsonMsg(jsonMsg);
        String data = toJSONString(jsonMsg);
        return sendDelayMsg(topic, tag, data, null, delayLevelEnum);
    }

    public boolean sendDelayMsg(String tag, String msg, DelayLevelEnum delayLevelEnum) {
        return sendDelayMsg(topic, tag, msg, null, delayLevelEnum);
    }

    public boolean sendDelayMsg(String tag, String msg, String key, DelayLevelEnum delayLevelEnum) {
        return sendDelayMsg(topic, tag, msg, key, delayLevelEnum);
    }

    private boolean sendDelayMsg(String topic, String tag, String data, String key, DelayLevelEnum delayLevelEnum) {
        if (Strings.isNullOrEmpty(topic)) {
            throw new IllegalArgumentException("topic is null or empty");
        }
        try {
            Message msg = new Message(topic + envSuffix, tag, data.getBytes(StandardCharsets.UTF_8));
            msg.setDelayTimeLevel(delayLevelEnum.getLevel());
            msg.setKeys(key);
            int timeout = config.getSendMsgTimeout();
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
    // ------------- send delay end

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
