package com.pit.kafka.consumer;

import com.pit.kafka.entity.MessageEvent;

import java.util.List;

public interface MessageConsumer {
    void consume(MessageEvent msg);

    void consumeAll(List<MessageEvent> msgList);

    /**
     * 和配置文件里的consumerId绑定
     * @return
     */
    String topic();
}
