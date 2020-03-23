package com.andrew.common.log;

/**
 * @Description
 *
 * @Author Andrew
 * @Date 2020-03-20
 */
public class LogUtil {
	public static void debug(org.slf4j.Logger log, String msg) {
		if (log.isDebugEnabled()) {
			log.debug(msg);
		}
	}

	public static void debug(org.slf4j.Logger log, String msg, Object... args){
		if (log.isDebugEnabled()) {
			log.debug(msg, args);
		}
	}
}
