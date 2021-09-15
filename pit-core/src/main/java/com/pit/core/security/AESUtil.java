package com.pit.core.security;

import com.pit.core.math.RandomUtils2;
import com.pit.core.text.Charsets2;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * NacosConfig
 *
 * @author gy
 * @date 2020/3/19
 */
public class AESUtil {
    private static final String AES_ALG = "AES";
    //    private static final String AES_CBC_ALG = "AES/CBC/PKCS5Padding";
    private static final String AES_CBC_ALG = "AES/GCM/NoPadding";

    private static final int DEFAULT_AES_KEYSIZE = 128;
    private static final int DEFAULT_IVSIZE = 16;

    private static SecureRandom random = RandomUtils2.secureRandom();

    /**
     * 使用AES加密原始字符串.
     *
     * @param input 原始输入字符数组
     * @param key   符合AES要求的密钥
     */
    public static byte[] aesEncrypt(byte[] input, byte[] key) throws GeneralSecurityException {
        return aes(input, key, Cipher.ENCRYPT_MODE);
    }

    /**
     * 使用AES加密原始字符串.
     *
     * @param input 原始输入字符数组
     * @param key   符合AES要求的密钥
     * @param iv    初始向量
     */
    public static byte[] aesEncrypt(byte[] input, byte[] key, byte[] iv) throws GeneralSecurityException {
        return aes(input, key, iv, Cipher.ENCRYPT_MODE);
    }

    /**
     * 使用AES解密字符串, 返回原始字符串.
     *
     * @param input Hex编码的加密字符串
     * @param key   符合AES要求的密钥
     */
    public static String aesDecrypt(byte[] input, byte[] key) throws GeneralSecurityException {
        byte[] decryptResult = aes(input, key, Cipher.DECRYPT_MODE);
        return new String(decryptResult, Charsets2.UTF_8);
    }

    /**
     * 使用AES解密字符串, 返回原始字符串.
     *
     * @param input Hex编码的加密字符串
     * @param key   符合AES要求的密钥
     * @param iv    初始向量
     */
    public static String aesDecrypt(byte[] input, byte[] key, byte[] iv) throws GeneralSecurityException {
        byte[] decryptResult = aes(input, key, iv, Cipher.DECRYPT_MODE);
        return new String(decryptResult, Charsets2.UTF_8);
    }

    /**
     * 使用AES加密或解密无编码的原始字节数组, 返回无编码的字节数组结果.
     *
     * @param input 原始字节数组
     * @param key   符合AES要求的密钥
     * @param mode  Cipher.ENCRYPT_MODE 或 Cipher.DECRYPT_MODE
     */
    private static byte[] aes(byte[] input, byte[] key, int mode) throws GeneralSecurityException {
        SecretKey secretKey = new SecretKeySpec(key, AES_ALG);
        Cipher cipher = Cipher.getInstance(AES_ALG);
        cipher.init(mode, secretKey);
        return cipher.doFinal(input);
    }

    /**
     * 使用AES加密或解密无编码的原始字节数组, 返回无编码的字节数组结果.
     *
     * @param input 原始字节数组
     * @param key   符合AES要求的密钥
     * @param iv    初始向量
     * @param mode  Cipher.ENCRYPT_MODE 或 Cipher.DECRYPT_MODE
     */
    private static byte[] aes(byte[] input, byte[] key, byte[] iv, int mode) throws GeneralSecurityException {

        SecretKey secretKey = new SecretKeySpec(key, AES_ALG);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(AES_CBC_ALG);
        cipher.init(mode, secretKey, ivSpec);
        return cipher.doFinal(input);

    }

    /**
     * 生成AES密钥,返回字节数组, 默认长度为128位(16字节).
     */
    public static byte[] generateAesKey() throws GeneralSecurityException {
        return generateAesKey(DEFAULT_AES_KEYSIZE);
    }

    /**
     * 生成AES密钥,可选长度为128,192,256位.
     */
    public static byte[] generateAesKey(int keySize) throws GeneralSecurityException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALG);
        keyGenerator.init(keySize);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * 生成随机向量,默认大小为cipher.getBlockSize(), 16字节.
     */
    public static byte[] generateIV() {
        byte[] bytes = new byte[DEFAULT_IVSIZE];
        random.nextBytes(bytes);
        return bytes;
    }
}
