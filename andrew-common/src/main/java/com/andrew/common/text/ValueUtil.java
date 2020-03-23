package com.andrew.common.text;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Andrew
 * @date 2020/3/23
 */
public class ValueUtil {
	public static String getOrDefault(String in, String def) {
		return StringUtils.isBlank(in) ? def : in;
	}

	public static int getOrDefault(Integer in, int def) {
		return null == in ? def : in;
	}
}
