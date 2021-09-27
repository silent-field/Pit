package com.pit.kafka.config;

import lombok.Data;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/26
 */
@Data
public class ProducerConfig {
    /**
     * 生产者Id
     */
    private String producerId;

    private String topic;

    /**
     * 生产者发送失败后，重试的次数
     */
    private Integer retries = 3;

    /**
     * 当多条消息发送到同一个partition时，该值控制生产者批量发送消息的大小，批量发送可以减少生产者到服务端的请求数，有助于提高客户端和服务端的性能。
     */
    private Integer batchSize = 16384;

    /**
     * batch.size和linger.ms是两种实现让客户端每次请求尽可能多的发送消息的机制，它们可以并存使用，并不冲突。
     * 为减少负载和客户端的请求数量，生产者不会一条一条发送，而是会逗留一段时间批量发送。batch.size和linger.ms满足任何一个条件都会发送。
     */
    private Integer lingerMs = 1;

    /**
     * 生产者最大可用缓存 (默认：33554432，32M)
     * 生产者可以用来缓冲等待发送到服务器的记录的总内存字节。如果记录被发送的速度超过了它们可以被发送到服务器的速度，那么生产者将阻塞max.block。然后它会抛出一个异常。
     */
    private Integer bufferMemory = 33554432;

    /**
     * acks=0：设置为0，则生产者将完全不等待来自服务器的任何确认。记录将立即添加到socket缓冲区，并被认为已发送。在这种情况下，不能保证服务器已经收到记录，
     * 重试配置将不会生效(因为客户机通常不会知道任何失败)。每个记录返回的偏移量总是-1。
     * <br>
     * acks=1:leader会将记录写到本地日志中，但不会等待所有follower的完全确认。在这种情况下，如果leader在记录失败后立即失败，但在追随者复制记录之前失败，那么记录就会丢失。
     * <br>
     * acks=all / -1:leader将等待完整的同步副本来确认记录。这保证了只要至少有一个同步副本仍然存在，记录就不会丢失。这是最有力的保证。这相当于acks=-1设置。
     */
    private String acks = "1";
}
