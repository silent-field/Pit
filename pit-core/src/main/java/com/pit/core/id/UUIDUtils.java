package com.pit.core.id;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 生成uuid
 *
 * @author gy
 * @date 2020/3/20
 */
public class UUIDUtils {
	public static String getUUID() {
		return new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong()).toString() + Thread.currentThread().getId();
	}
}
