package com.andrew.common.dict;

/**
 * 字典类接口
 *
 * @Author Andrew
 * @Date 2019-06-15 12:06
 */
public interface Dict<C, D> {
	/**
	 * 标识
	 * @return
	 */
	C getCode();

	/**
	 * 描述
	 * @return
	 */
	D getDesc();
}
