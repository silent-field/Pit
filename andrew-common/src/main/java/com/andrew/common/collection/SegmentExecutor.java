package com.andrew.common.collection;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.*;

/**
 * 对input Collection<I> 分partition，对每个partition执行SegmentExecuteHandler<I, T>，得到Collection<T>
 *
 * @Author Andrew
 * @Date 2019-06-15 12:06
 */
public class SegmentExecutor {
	public static <I, T> List<T> executeWithList(List<I> input, int segmentSize,
												 ListSegmentExecuteHandler<I, T> handler) {
		if (CollectionUtils.isEmpty(input)) {
			return Collections.EMPTY_LIST;
		}
		List<List<I>> partitions = Lists.partition(input, segmentSize);
		List<T> result = Lists.newArrayList();
		for (List<I> partition : partitions) {
			List<T> partitionResult = handler.execute(partition);
			if (CollectionUtils.isNotEmpty(partitionResult)) {
				result.addAll(partitionResult);
			}
		}
		return result;
	}

	public static <T> List<T> executeWithNumber(int input, int segmentSize, NumberSegmentExecuteHandler<T> handler) {
		if (input <= 0) {
			return Collections.EMPTY_LIST;
		}

		List<T> result = Lists.newArrayList();
		int current = 0;
		int temp = input;
		for (; ; ) {
			if (temp <= 0) {
				break;
			}

			int limit = 0;
			if (temp <= segmentSize) {
				limit = temp;
			} else {
				limit = segmentSize;
			}

			List<T> partitionResult = handler.execute(current, limit);
			if (CollectionUtils.isNotEmpty(partitionResult)) {
				result.addAll(partitionResult);
			}

			current += limit;
			temp -= limit;
		}

		return result;
	}

	public static <I, K, V> Map<K, V> executeWithList(List<I> input, int segmentSize,
													  MapSegmentExecuteHandler<I, K, V> handler) {
		if (CollectionUtils.isEmpty(input)) {
			return Collections.EMPTY_MAP;
		}
		List<List<I>> partitions = Lists.partition(input, segmentSize);
		Map<K, V> result = new HashMap<>();
		for (List<I> partition : partitions) {
			Map<K, V> partitionResult = handler.execute(partition);
			if (MapUtils.isNotEmpty(partitionResult)) {
				result.putAll(partitionResult);
			}
		}
		return result;
	}

	public interface ListSegmentExecuteHandler<I, T> {
		List<T> execute(List<I> input);
	}

	public interface MapSegmentExecuteHandler<I, K, V> {
		Map<K, V> execute(List<I> input);
	}

	public interface NumberSegmentExecuteHandler<T> {
		List<T> execute(int start, int limit);
	}

	public static void main(String[] args) {
		SegmentExecutor.executeWithNumber(25, 4, new NumberSegmentExecuteHandler<String>() {
			@Override
			public List<String> execute(int start, int limit) {
				System.out.print("start:" + start + ",limit:" + limit + "--------");
				for (int i = 0; i < limit; i++) {
					System.out.print((start + i + 1) + ",");
				}
				System.out.println("");
				return new ArrayList<>();
			}
		});
	}
}