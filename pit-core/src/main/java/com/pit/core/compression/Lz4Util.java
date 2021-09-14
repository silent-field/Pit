package com.pit.core.compression;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

/**
 * @author gy
 * @version 1.0
 * @date 2020/7/13.
 */
public class Lz4Util {
    private static LZ4SafeDecompressor decompressor = LZ4Factory.fastestJavaInstance().safeDecompressor();

    /**
     * 将 String 反序列化为原对象
     * 利用了 Base64 编码
     *
     * @param str   writeToString 方法序列化后的字符串
     * @param clazz 原对象的 Class
     * @param <T>   原对象的类型
     * @return 原对象
     */
    /**
     *
     * @param src
     * @return
     */
    public static byte[] lz4Decompress(byte[] src) {
        int size = src.length;
        // dest len
        byte[] lenbyte = new byte[4];
        System.arraycopy(src, (size - 4), lenbyte, 0, 4);
        int len = byteArrayToInt(lenbyte);

        // dest
        byte[] dest = new byte[len];
        decompressor.decompress(src, 0, (size - 4), dest, 0);
        return dest;
    }

    private static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
    }
}
