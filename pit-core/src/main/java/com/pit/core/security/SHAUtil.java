package com.pit.core.security;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * SHAUtil
 *
 * @author gy
 * @date 2020/3/19
 */
public class SHAUtil {
    private static final String HMACSHA1_ALG = "HmacSHA1";

    private static final int DEFAULT_HMACSHA1_KEYSIZE = 160; // RFC2401

    // -- HMAC-SHA1 funciton --//

    /**
     * 使用HMAC-SHA1进行消息签名, 返回字节数组,长度为20字节.
     *
     * @param input 原始输入字符数组
     * @param key   HMAC-SHA1密钥
     */
    public static byte[] hmacSha1(byte[] input, byte[] key) throws GeneralSecurityException {
        SecretKey secretKey = new SecretKeySpec(key, HMACSHA1_ALG);
        Mac mac = Mac.getInstance(HMACSHA1_ALG);
        mac.init(secretKey);
        return mac.doFinal(input);
    }

    /**
     * 校验HMAC-SHA1签名是否正确.
     *
     * @param expected 已存在的签名
     * @param input    原始输入字符串
     * @param key      密钥
     */
    public static boolean isMacValid(byte[] expected, byte[] input, byte[] key) throws GeneralSecurityException {
        byte[] actual = hmacSha1(input, key);
        return Arrays.equals(expected, actual);
    }

    /**
     * 生成HMAC-SHA1密钥,返回字节数组,长度为160位(20字节). HMAC-SHA1算法对密钥无特殊要求, RFC2401建议最少长度为160位(20字节).
     */
    public static byte[] generateHmacSha1Key() throws GeneralSecurityException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(HMACSHA1_ALG);
        keyGenerator.init(DEFAULT_HMACSHA1_KEYSIZE);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }
}
