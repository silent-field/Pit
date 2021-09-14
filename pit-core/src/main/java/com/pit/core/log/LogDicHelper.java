package com.pit.core.log;

import java.util.Set;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/12.
 */
public interface LogDicHelper {
	/**
	 * 返回无需打印日志的关键字
	 *
	 * @return
	 */
	Set<String> notLogStartKeys();
}
