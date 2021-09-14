package com.pit.core.compression;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.pit.core.text.Charsets2;
import org.apache.commons.codec.binary.Base64;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Kryo 序列化工具类
 *
 * @author gy
 * @date 2020/3/20
 */
public class KryoUtil {
    /**
     * 每个线程持有各自的 Kryo 实例，Kryo非线程安全
     */
    private static final ThreadLocal<Kryo> kryoLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();

            /**
             * 不要轻易改变这里的配置！更改之后，序列化的格式就会发生变化，
             * 上线的同时就必须清除 Redis 里的所有缓存，
             * 否则那些缓存再回来反序列化的时候，就会报错
             */

            // 支持对象循环引用（否则会栈溢出）
            // 默认值就是 true，添加此行的目的是为了提醒维护者，不要改变这个配置
            kryo.setReferences(true);

            // 不强制要求注册类（注册行为无法保证多个 JVM 内同一个类的注册编号相同；而且业务系统中大量的 Class 也难以一一注册）
            // 默认值就是 false，添加此行的目的是为了提醒维护者，不要改变这个配置
            kryo.setRegistrationRequired(false);

            // Fix the NPE bug when deserializing Collections.
            ((DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
                    .setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());

            return kryo;
        }
    };

    /**
     * 获得当前线程的 Kryo 实例
     *
     * @return 当前线程的 Kryo 实例
     */
    public static Kryo getInstance() {
        return kryoLocal.get();
    }

    // -----------------------------------------------
    // 序列化/反序列化对象，及类型信息
    // 序列化的结果里，包含类型的信息
    // 反序列化时不再需要提供类型
    // -----------------------------------------------
//
//    /**
//     * 将对象【及类型】序列化为字节数组
//     *
//     * @param obj 任意对象
//     * @param <T> 对象的类型
//     * @return 序列化后的字节数组
//     */
//    @Deprecated
//    public static <T> byte[] writeToByteArray(T obj) {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        Output output = new Output(byteArrayOutputStream);
//
//        Kryo kryo = getInstance();
//        kryo.writeClassAndObject(output, obj);
//        output.flush();
//
//        return byteArrayOutputStream.toByteArray();
//    }
//
//    /**
//     * 将对象【及类型】序列化为 String
//     * 利用了 Base64 编码
//     *
//     * @param obj 任意对象
//     * @param <T> 对象的类型
//     * @return 序列化后的字符串
//     */
//    @Deprecated
//    public static <T> String writeToString(T obj) {
//        try {
//            return new String(Base64.encodeBase64(writeToByteArray(obj)), DEFAULT_ENCODING);
//        } catch (UnsupportedEncodingException e) {
//            throw new IllegalStateException(e);
//        }
//    }
//
//    /**
//     * 将字节数组反序列化为原对象
//     *
//     * @param byteArray writeToByteArray 方法序列化后的字节数组
//     * @param <T>       原对象的类型
//     * @return 原对象
//     */
//    @SuppressWarnings("unchecked")
//    @Deprecated
//    public static <T> T readFromByteArray(byte[] byteArray) {
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
//        Input input = new Input(byteArrayInputStream);
//
//        Kryo kryo = getInstance();
//        return (T) kryo.readClassAndObject(input);
//    }
//
//    /**
//     * 将 String 反序列化为原对象
//     * 利用了 Base64 编码
//     *
//     * @param str writeToString 方法序列化后的字符串
//     * @param <T> 原对象的类型
//     * @return 原对象
//     */
//    public static <T> T readFromString(String str) {
//        try {
//            return readFromByteArray(Base64.decodeBase64(str.getBytes(DEFAULT_ENCODING)));
//        } catch (UnsupportedEncodingException e) {
//            throw new IllegalStateException(e);
//        }
//    }

    // -----------------------------------------------
    // 只序列化/反序列化对象
    // 序列化的结果里，不包含类型的信息
    // -----------------------------------------------

    /**
     * 将对象序列化为字节数组
     *
     * @param obj 任意对象
     * @param <T> 对象的类型
     * @return 序列化后的字节数组
     */
    public static <T> byte[] writeObjectToByteArray(T obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);

        Kryo kryo = getInstance();
        kryo.writeObject(output, obj);
        output.flush();

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 将对象序列化为 String
     * 利用了 Base64 编码
     *
     * @param obj 任意对象
     * @param <T> 对象的类型
     * @return 序列化后的字符串
     */
    public static <T> String writeObjectToString(T obj) {
        try {
            return new String(Base64.encodeBase64(writeObjectToByteArray(obj)), Charsets2.UTF_8_NAME);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 将字节数组反序列化为原对象
     *
     * @param byteArray writeToByteArray 方法序列化后的字节数组
     * @param clazz     原对象的 Class
     * @param <T>       原对象的类型
     * @return 原对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T readObjectFromByteArray(byte[] byteArray, Class<T> clazz) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        Input input = new Input(byteArrayInputStream);

        Kryo kryo = getInstance();
        return kryo.readObject(input, clazz);
    }

    /**
     * 将 String 反序列化为原对象
     * 利用了 Base64 编码
     *
     * @param str   writeToString 方法序列化后的字符串
     * @param clazz 原对象的 Class
     * @param <T>   原对象的类型
     * @return 原对象
     */
    public static <T> T readObjectFromString(String str, Class<T> clazz) {
        try {
            return readObjectFromByteArray(Base64.decodeBase64(str.getBytes(Charsets2.UTF_8_NAME)), clazz);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}