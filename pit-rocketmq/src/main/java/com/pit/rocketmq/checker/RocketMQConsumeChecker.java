package com.pit.rocketmq.checker;

import com.pit.core.limit.CountLimiterGrantWhitTime;
import com.pit.core.text.StringUtils2;
import com.pit.rocketmq.consumer.DefaultRocketMQConsumer;
import com.pit.rocketmq.factory.RocketMQFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/26
 */
@Slf4j
public class RocketMQConsumeChecker {
    // 默认30秒采集一次数据
    private CountLimiterGrantWhitTime countLimit = new CountLimiterGrantWhitTime(1, 30000, TimeUnit.MILLISECONDS);

    public static final int DEFAULT_THRESHOLD = 1000;

    // 默认积压达到1000就告警
    private int backlogThreshold = DEFAULT_THRESHOLD;

    private RocketMQFactory rocketMQFactory;

    public RocketMQConsumeChecker(RocketMQFactory rocketMQFactory) {
        this.rocketMQFactory = rocketMQFactory;
    }

    /**
     * 检查积压告警，try-catch所有异常
     */
    public void checkBacklogQuietly() {
        if (rocketMQFactory != null && MapUtils.isNotEmpty(rocketMQFactory.getConsumers())) {
            long curTime = System.currentTimeMillis();
            boolean grant = countLimit.grant(curTime);
            if (grant) {
                StringBuffer errMsg = new StringBuffer();
                try {
                    for (DefaultRocketMQConsumer consumer : rocketMQFactory.getConsumers().values()) {
                        MessageExt msg = consumer.getNewest();
                        /**
                         * 积压告警
                         */
                        long queueOffset = msg.getQueueOffset();
                        long maxOffset = Long.parseLong(msg.getProperties().get(MessageConst.PROPERTY_MAX_OFFSET));
                        long backlog = maxOffset - queueOffset;
                        // 毫秒值
                        Long storeTimestamp = msg.getStoreTimestamp();

                        log.info("消费信息采样：topic[{}],消息[{}],消息生产时间:[{}],queueId[{}],queueOffset[{}],maxOffset是[{}],当前消费组的积压量:[{}]",
                                msg.getTopic(), new String(msg.getBody()), storeTimestamp, msg.getQueueId(), queueOffset,
                                maxOffset, backlog);

                        // 检查积压数告警
                        if (backlog >= backlogThreshold) {
                            errMsg.append(StringUtils2.format("消费信息采样发现消费积压：topic[{}],消息生产时间:{},queueId[{}],queueOffset[{}],maxOffset是[{}],当前消费组的积压量:[{}]",
                                    msg.getTopic(), storeTimestamp, msg.getQueueId(), queueOffset, maxOffset, backlog));
                        }
                    }
                } catch (Exception e) {
                    log.error("消费积压检查发生异常", e);
                }

                if (errMsg.length() > 0) {
                    log.warn(errMsg.toString());
                }
            }

        }
    }
}
