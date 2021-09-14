package com.pit.core.text;

import com.google.common.io.BaseEncoding;

/**
 * 使用 guava {@linkplain BaseEncoding} 对字符串进行16进制、base64 编解码，对url param进行 base64 编解码
 * <p>
 * 传统的Base64用的是A-Z、a-z、0-9，还有+和/，一共64个编码串。
 * 由于URL编码器会把标准Base64中的“/”和“+”字符变为形如“%XX”的形式，而这些“%”号在存入数据库时还需要再进行转换，这是因为ANSI SQL中已将“%”号用作通配符。
 * 为解决此问题，可采用一种用于URL的改进Base64编码，它不在末尾填充’=’号，并将标准Base64中的“+”和“/”分别改成了“-”和“_”，这样就免去了在URL编解码和数据库存储时所要作的转换。
 * see RFC3548 https://tools.ietf.org/html/rfc3548#section-4
 * </p>
 *
 * @author gy
 * @date 2020/3/20
 */
public class EncodeUtil {

    /**
     * 将16进制的byte[]编码为String
     *
     * @param input
     * @return
     */
    public static String encodeHex(byte[] input) {
        return BaseEncoding.base16().encode(input);
    }

    /**
     * 将String解码为16进制的byte[]
     *
     * @param input
     * @return
     */
    public static byte[] decodeHex(CharSequence input) {
        return BaseEncoding.base16().decode(input);
    }

    /**
     * 将base64编码的byte[]编码为String
     *
     * @param input
     * @return
     */
    public static String encodeBase64(byte[] input) {
        return BaseEncoding.base64().encode(input);
    }

    /**
     * 将String解码为base64的byte[]
     *
     * @param input
     * @return
     */
    public static byte[] decodeBase64(CharSequence input) {
        return BaseEncoding.base64().decode(input);
    }

    /**
     * 将base64编码的byte[] url编码为String
     * Base64中的URL非法字符'+'和'/'会被转为'-'和'_', 见RFC3548
     *
     * @param input
     * @return
     */
    public static String encodeBase64Url(byte[] input) {
        return BaseEncoding.base64Url().encode(input);
    }

    /**
     * 将String url解码为base64的byte[]
     * 需要将Base64中的URL非法字符'+'和'/'转为'-'和'_', 见RFC3548
     *
     * @param input
     * @return
     */
    public static byte[] decodeBase64Url(CharSequence input) {
        return BaseEncoding.base64Url().decode(input);
    }
}