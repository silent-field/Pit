package com.pit.core.codec;

/**
 * @author gy
 * @version 1.0
 * @date 2020/7/13.
 */
public interface Unmarshallable {
	/**
	 * 反序列化
	 *
	 * @param unpack
	 */
	void unmarshal(TcpUnpack unpack);
}
