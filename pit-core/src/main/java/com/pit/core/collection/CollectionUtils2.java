package com.pit.core.collection;

import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/17.
 * @description:
 */
public class CollectionUtils2 {
	/**
	 * 如果提供的集合为{@code null}，返回一个不可变的默认空集合，否则返回原集合<br>
	 *
	 * @param input
	 * @param <T>
	 * @return
	 */
	public static <T> Set<T> emptyIfNull(Set<T> input) {
		return (null == input) ? Collections.emptySet() : input;
	}

	/**
	 * 如果提供的集合为{@code null}，返回一个不可变的默认空集合，否则返回原集合<br>
	 *
	 * @param input
	 * @param <T>
	 * @return
	 */
	public static <T> List<T> emptyIfNull(List<T> input) {
		return (null == input) ? Collections.emptyList() : input;
	}

	/**
	 * 多个集合去重合并
	 *
	 * @param collections
	 * @param <T>
	 * @return
	 */
	@SafeVarargs
	public static <T> Set<T> distinctAll(Collection<T>... collections) {
		final Set<T> result = new HashSet<>();

		if (ArrayUtils.isNotEmpty(collections)) {
			for (Collection<T> coll : collections) {
				result.addAll(coll);
			}
		}

		return result;
	}

	/**
	 * 多个集合并集，不去重
	 *
	 * @param collections
	 * @param <T>
	 * @return
	 */
	@SafeVarargs
	public static <T> List<T> unionAll(Collection<T>... collections) {
		final List<T> result = new ArrayList<>();

		if (ArrayUtils.isNotEmpty(collections)) {
			for (Collection<T> coll : collections) {
				result.addAll(coll);
			}
		}

		return result;
	}

	/**
	 * 两个集合的交集<br>
	 *
	 * @param <T>
	 * @return
	 */
	public static <T> Set<T> intersection(Collection<T>... collections) {
		if (ArrayUtils.isNotEmpty(collections)) {
			return new HashSet<>();
		}

		for (Collection<T> coll : collections) {
			if (CollectionUtils.isEmpty(coll)) {
				return new HashSet<>();
			}
		}

		Set<T> intersection = new HashSet<>(collections[0]);
		for (int i = 1; i < collections.length; i++) {
			intersection = Sets.intersection(intersection, new HashSet<>(collections[i]));
		}

		return intersection;
	}
}
