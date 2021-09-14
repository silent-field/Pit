package com.pit.ext.object;

import org.apache.lucene.util.RamUsageEstimator;

import java.util.HashSet;
import java.util.Set;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/12.
 */
public class CalcSizeByRamUsageEstimator {
	public static void printObjectSize(Object obj){
		// 计算指定对象及其引用树上的所有对象的综合大小，单位字节
		long sizeOf = RamUsageEstimator.sizeOf(obj);

		// 计算指定对象本身在堆空间的大小，单位字节
		long shallowSizeOf = RamUsageEstimator.shallowSizeOf(obj);

		// 计算指定对象及其引用树上的所有对象的综合大小，返回可读的结果，如：2KB
		String  humanSizeOf = RamUsageEstimator.humanSizeOf(obj);

		System.out.println("sizeOf : " + sizeOf );
		System.out.println("shallowSizeOf : " + shallowSizeOf);
		System.out.println("humanSizeOf : " + humanSizeOf);
	}

	public static void main(String[] args) {
		Set<Long> obj = new HashSet<>();
		for (long i = 0; i < 1000000; i++) {
			obj.add(i);
		}

		printObjectSize(obj);
	}
}
