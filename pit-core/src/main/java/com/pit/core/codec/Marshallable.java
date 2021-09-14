package com.pit.core.codec;

/**
 * @author gy
 * @version 1.0
 * @date 2020/7/13.
 */
public interface Marshallable {
    /**
     * 序列化
     *
     * @param pack
     */
    void marshall(TcpPack pack);
}
