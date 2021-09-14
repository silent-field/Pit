package com.pit.core.math;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author gy
 * @version 1.0
 * @date 2020/7/23.
 */
public class NumberUtils2 {
	/**
	 * 是否存在非正数的整数，0或负数
	 *
	 * @param numbers
	 * @return
	 */
	public static boolean isAnyNonPositive(Number... numbers) {
		if (ArrayUtils.isEmpty(numbers)) {
			return false;
		}

		for (Number number : numbers) {
			if (number.longValue() <= 0) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 是否存在负数
	 *
	 * @param numbers
	 * @return
	 */
	public static boolean isAnyNegative(Number... numbers) {
		if (ArrayUtils.isEmpty(numbers)) {
			return false;
		}

		for (Number number : numbers) {
			if (number.longValue() < 0) {
				return true;
			}
		}

		return false;
	}
}
