package com.pit.kafka.producer;

import com.pit.kafka.config.ProducerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.Future;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/27
 */
@Slf4j
public class DefaultKafkaProducer {
    private ProducerConfig config;
    private String bootstrapServers;
    private String envSuffix;
    private String topic;
    private String topicWithEnv;

    private KafkaProducer<String, String> producer;

    public DefaultKafkaProducer(String bootstrapServers, String env, ProducerConfig config) {
        this.config = config;

        if (StringUtils.isBlank(bootstrapServers)) {
            log.error("kafkaProducer's bootstrapServers is null, init failed");
            return;
        }
        this.bootstrapServers = bootstrapServers;

        if (StringUtils.isBlank(config.getTopic())) {
            log.error("kafkaProducer's topic is null, init failed");
            return;
        }

        envSuffix = StringUtils.isBlank(env) ? "" : "_" + env;
        topic = config.getTopic();

        topicWithEnv = topic + envSuffix;
    }

    public void start() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("acks", config.getAcks());
        props.put("retries", config.getRetries());
        props.put("batch.size", config.getBatchSize());
        /**
         * 默认情况下缓冲区的消息会被立即发送到服务端，即使缓冲区的空间并没有被用完。可以将该值设置为大于0的值，这样发送者将等待一段时间后，
         * 再向服务端发送请求，以实现每次请求可以尽可能多的发送批量消息。
         * batch.size和linger.ms是两种实现让客户端每次请求尽可能多的发送消息的机制，它们可以并存使用，并不冲突。
         */
        props.put("linger.ms", config.getLingerMs());
        props.put("buffer.memory", config.getBufferMemory());
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
    }

    public synchronized void stop() {
        if (producer != null) {
            producer.close();
        }
    }

    /**
     * 异步发送消息
     *
     * @param message
     * @param callback       可选
     * @return
     */
    public Future<RecordMetadata> sendMessageAsync(String message, Callback callback) {
        ProducerRecord<String, String> msg = new ProducerRecord<>(topicWithEnv, message);
        return producer.send(msg, callback);
    }

    /**
     * 同步发送消息，阻塞等待结果
     *
     * @return
     */
    public void sendMessage(String message) {
        ProducerRecord<String, String> msg = new ProducerRecord<>(topicWithEnv, message);
        producer.send(msg);
    }
}
