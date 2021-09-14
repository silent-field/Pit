package com.pit.service.orchestration;

import java.util.Map;

/**
 * @author gy
 * @version 1.0
 * @description:
 * @date 2020/9/16.
 */
public interface IRequestHook {
	/**
	 * 方法执行前调用
	 *
	 * @param url
	 * @param headers
	 * @param income
	 * @param output
	 * @return
	 * @throws Exception
	 */
	default Object doBefore(String url, Map<String, String> headers, Object[] income, Object output) throws Exception {
		return null;
	}

	/**
	 * 方法执行后调用
	 *
	 * @param url
	 * @param headers
	 * @param income
	 * @param output
	 * @return
	 * @throws Exception
	 */
	default Object doAfter(String url, Map<String, String> headers, Object[] income, Object output) throws Exception {
		return output;
	}
}
