package com.pit.core.executor;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.*;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/18.
 * @description: 分段执行器
 */
public class SegmentExecutor {
    /**
     * 将 input(list) 根据 segmentSize 分段后调用执行 {@linkplain ListSegmentExecuteHandler#execute(List)} </br>
     * 结果返回 List
     *
     * @param input
     * @param segmentSize
     * @param handler
     * @param <I>
     * @param <T>
     * @return
     */
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

    /**
     * 将 input(int) 根据 segmentSize 分段后调用执行 {@linkplain NumberSegmentExecuteHandler#execute(int, int)} </br>
     * 结果返回 List
     *
     * @param input
     * @param segmentSize
     * @param handler
     * @param <T>
     * @return
     */
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

    /**
     * 根据 segmentSize 分批调用执行 {@linkplain NumberSegmentExecuteHandler#execute(int, int)} </br>
     * 直到 NumberSegmentExecuteHandler#execute(int, int) 返回结果为空或者小于 segmentSize
     *
     * @param segmentSize
     * @param handler
     * @param <T>
     * @return
     */
    public static <T> List<T> executeWithUnlimited(int segmentSize, NumberSegmentExecuteHandler<T> handler) {
        if (segmentSize <= 0) {
            return Collections.EMPTY_LIST;
        }

        List<T> result = Lists.newArrayList();
        int current = 0;
        int limit = segmentSize;
        for (; ; ) {
            List<T> partitionResult = handler.execute(current, limit);
            if (CollectionUtils.isNotEmpty(partitionResult)) {
                result.addAll(partitionResult);
            }

            if (CollectionUtils.isEmpty(partitionResult) || partitionResult.size() < limit) {
                break;
            }

            current += limit;
        }

        return result;
    }

    /**
     * 将 input(int) 根据 segmentSize 分段后调用执行 {@linkplain MapSegmentExecuteHandler#execute(List)} </br>
     * 结果返回 Map
     *
     * @param input
     * @param segmentSize
     * @param handler
     * @param <I>
     * @param <K>
     * @param <V>
     * @return
     */
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

    public interface ListSegmentExecuteHandler<I, T> {
        /**
         * 处理 input
         *
         * @param input
         * @return
         */
        List<T> execute(List<I> input);
    }

    public interface MapSegmentExecuteHandler<I, K, V> {
        /**
         * 处理 input
         *
         * @param input
         * @return
         */
        Map<K, V> execute(List<I> input);
    }

    public interface NumberSegmentExecuteHandler<T> {
        /**
         * 根据start 、 limit 处理
         *
         * @param start
         * @param limit
         * @return
         */
        List<T> execute(int start, int limit);
    }
}
