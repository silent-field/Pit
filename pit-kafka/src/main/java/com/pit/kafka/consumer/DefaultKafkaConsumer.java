package com.pit.kafka.consumer;

import com.google.common.util.concurrent.MoreExecutors;
import com.pit.kafka.config.ConsumerConfig;
import com.pit.kafka.entity.MessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/27
 */
@Slf4j
public class DefaultKafkaConsumer implements Runnable {
    private ConsumerConfig config;
    private String bootstrapServers;
    private String envSuffix;
    private String topic;
    private String topicWithEnv;
    private String consumerDesc;

    private KafkaConsumer<String, String> consumer;
    private MessageConsumer messageConsumer;

    private ThreadPoolExecutor consumeThreadPoolExecutor;
    private volatile boolean running = true;
    private Object shutdownLck = new Object();

    /**
     * 初始化配置文件里面的各种消费者属性配置
     * @param
     */
    public DefaultKafkaConsumer(String bootstrapServers, String env, ConsumerConfig config, MessageConsumer messageConsumer) {
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

        if (StringUtils.isBlank(config.getGroupId())) {
            log.error("kafkaProducer's groupId is null, init failed");
            return;
        }

        envSuffix = StringUtils.isBlank(env) ? "" : "_" + env;
        topic = config.getTopic();

        topicWithEnv = topic + envSuffix;
        this.messageConsumer = messageConsumer;
    }

    public void start() {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", bootstrapServers);
        props.setProperty("group.id", config.getGroupId() + envSuffix);
        if (StringUtils.isNotEmpty(config.getOffsetReset())) {
            props.put("auto.offset.reset", config.getOffsetReset());
        }
        consumerDesc = topicWithEnv + "@" + config.getTopic();

        String offset = config.getOffset();
        if (!StringUtils.isBlank(offset)) {
            props.put("auto.offset.reset", offset);
        }

        props.put("enable.auto.commit", config.isAutoCommit());
        props.put("auto.commit.interval.ms", "10000");
        props.put("max.poll.records", 100);

        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");



        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topicWithEnv));

        initThreadPool();

        Thread t1 = new Thread(this);
        t1.setName(consumer.getClass().getSimpleName() + "-main");
        t1.start();
        log.info("NewKafkaConsumer started consumer:{}", consumerDesc);
    }

    private void initThreadPool() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, topicWithEnv + "-" + threadNumber.incrementAndGet());
                return thread;
            }
        };

        consumeThreadPoolExecutor = new ThreadPoolExecutor(config.getCorePoolSize(),
                config.getMaxPoolSize(),
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(config.getQueueCapacity()), threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public synchronized void stop() {
        synchronized (shutdownLck) {
            running = false;
            if (consumeThreadPoolExecutor != null) {
                MoreExecutors.shutdownAndAwaitTermination(consumeThreadPoolExecutor, 5, TimeUnit.SECONDS);
            }
            try {
                shutdownLck.wait(3000L);
            } catch (InterruptedException e) {
                log.error("KafkaConsumer stop", e);
                Thread.currentThread().interrupt();
            }
        }
        log.info("KafkaConsumer stopped consumer:{}", consumerDesc);
    }

    @Override
    public void run() {
        try {
            while (running) {
                try {
                    // 1000ms内等待Kafka broker返回数据.不管有没有可用的数据都要返回
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000L));

                    ConsumerRecord<String, String> newest = null;
                    for (TopicPartition partition : records.partitions()) {
                        List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                        for (ConsumerRecord<String, String> record : partitionRecords) {
                            MessageEvent messageEvent = new MessageEvent(record.value());
                            messageEvent.putHeader(MessageEvent.TIMESTAMP, String.valueOf(System.currentTimeMillis()));
                            messageEvent.putHeader(MessageEvent.TOPIC, topicWithEnv);
                            messageEvent.putHeader(MessageEvent.PARTITION, record.partition());
                            messageEvent.putHeader(MessageEvent.OFFSET, record.offset());
                            if (record.key() != null) {
                                messageEvent.putHeader(MessageEvent.KEY, record.key());
                            }
                            CompletableFuture.runAsync(() -> {
                                messageConsumer.consume(messageEvent);
                            }, consumeThreadPoolExecutor).exceptionally(ex -> {
                                log.error(consumerDesc + " Kafka consume EXCEPTION", ex);
                                return null;
                            });
                            newest = record;
                        }

                        if (!config.isAutoCommit() && newest != null) {
                            // commit the read transactions to Kafka to avoid duplicates
                            consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(newest.offset() + 1)));
                            newest = null;
                        }
                    }
                } catch (Exception e) {
                    log.error(consumerDesc + " Kafka consume EXCEPTION ", e);
                }
            }
        } finally {
            consumer.close();
        }

        synchronized (shutdownLck) {
            shutdownLck.notifyAll();
        }
    }
}
