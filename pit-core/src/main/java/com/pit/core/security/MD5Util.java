package com.pit.core.security;

import java.security.MessageDigest;

/**
 * MD5Util
 *
 * @author gy
 * @date 2020/3/19
 */
public class MD5Util {
    public static String getMD5Str(String str) throws Exception {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            // 获得密文
            byte[] mdBytes = md.digest();
            // 把密文转换成十六进制的字符串形式
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < mdBytes.length; i++) {
                int tmp = mdBytes[i];
                if (tmp < 0) {
                    tmp += 256;
                }
                if (tmp < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(tmp));
            }
            return buf.toString().toLowerCase();
        } catch (Exception e) {
            throw new Exception("MD5 occurs exception，" + e.toString());
        }
    }
}
