package com.andrew.common.mq.kafka.producer;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 *
 * @Author Andrew
 * @Date 2020-3-20
 */
public class KafkaProducerPartitioner implements Partitioner {

	private final ConcurrentMap<String, AtomicInteger> topicCounterMap = new ConcurrentHashMap<>();

	@Override
	public void configure(Map<String, ?> configs) {
		// nothing to do
	}

	@Override
	public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
		List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
		int numPartitions = partitions.size();
		if (keyBytes == null) {
			int nextValue = nextValue(topic);
			List<PartitionInfo> availablePartitions = cluster.availablePartitionsForTopic(topic);
			if (availablePartitions.size() > 0) {
				int part = toPositive(nextValue) % availablePartitions.size();
				return availablePartitions.get(part).partition();
			} else {
				// no partitions are available, give a non-available partition
				return toPositive(nextValue) % numPartitions;
			}
		} else {
			// hash the keyBytes to choose a partition
			return toPositive(Utils.murmur2(keyBytes)) % numPartitions;
		}
	}
	
    /**
     * A cheap way to deterministically convert a number to a positive value. When the input is
     * positive, the original value is returned. When the input number is negative, the returned
     * positive value is the original value bit AND against 0x7fffffff which is not its absolutely
     * value.
     *
     * Note: changing this method in the future will possibly cause partition selection not to be
     * compatible with the existing messages already placed on a partition since it is used
     * in producer's {@link org.apache.kafka.clients.producer.internals.DefaultPartitioner}
     *
     * @param number a given number
     * @return a positive number.
     */
    public static int toPositive(int number) {
        return number & 0x7fffffff;
    }

	private int nextValue(String topic) {
		AtomicInteger counter = topicCounterMap.get(topic);
		if (null == counter) {
			counter = new AtomicInteger(ThreadLocalRandom.current().nextInt());
			AtomicInteger currentCounter = topicCounterMap.putIfAbsent(topic, counter);
			if (currentCounter != null) {
				counter = currentCounter;
			}
		}
		return counter.getAndIncrement();
	}

	@Override
	public void close() {
		// nothing to do
	}

}
