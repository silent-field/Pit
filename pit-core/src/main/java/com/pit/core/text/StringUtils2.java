package com.pit.core.text;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.helpers.MessageFormatter;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * 补充String常用方法
 *
 * @author gy
 * @date 2020/3/20
 */
public class StringUtils2 {
    private StringUtils2() {

    }

    /**
     * {@linkplain String#format(String, Object...)} 需要占位符%s的数量与Object...的数量一致，否则会报错。
     * 而{@linkplain MessageFormatter#arrayFormat(String, Object[])} 没有这个限制，且效率高于{@linkplain String#format(String, Object...)}
     *
     * @param messagePattern 占位符使用"{}"，而非"%s"
     * @param args
     * @return
     */
    public static String format(String messagePattern, Object... args) {
        if (StringUtils.isBlank(messagePattern)) {
            return StringUtils.EMPTY;
        }

        if (null == args || args.length == 0) {
            return messagePattern;
        }

        return MessageFormatter.arrayFormat(messagePattern, args).getMessage();
    }

    /**
     * JDK8 String缺少replace last
     *
     * @param s
     * @param sub
     * @param with
     * @return
     */
    public static String replaceLast(String s, char sub, char with) {
        if (s == null) {
            return null;
        }

        int index = s.lastIndexOf(sub);
        if (index == -1) {
            return s;
        }
        char[] str = s.toCharArray();
        str[index] = with;
        return new String(str);
    }

    /**
     * 将字符串转二进制(可指定字符集) <br>
     * 如果字符串中存在中文，必须指定字符集，否则可能由于操作系统配置不一致得到二进制结果不一样
     *
     * @param s
     * @param charset
     * @param separator
     * @return
     */
    public static String toBinary(String s, Charset charset, String separator) {
        byte[] bytes = charset == null ? s.getBytes() : s.getBytes(charset);
        // 分隔符可以是空格
        boolean isFill = StringUtils.isNotEmpty(separator);
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes) {
            int val = b;
            for (int i = 0; i < 8; i++) {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }

            if (isFill) {
                binary.append(separator);
            }
        }

        return binary.toString();
    }

    /**
     * 是否以指定字符串开头，忽略大小写
     *
     * @param str
     * @param prefix
     * @return
     */
    public static boolean startWithIgnoreCase(CharSequence str, CharSequence prefix) {
        return startWith(str, prefix, true);
    }

    /**
     * 是否以指定字符串开头<br>
     * 如果给定的字符串和开头字符串都为null则返回true，否则任意一个值为null返回false
     *
     * @param str
     * @param prefix
     * @param ignoreCase
     * @return
     */
    public static boolean startWith(CharSequence str, CharSequence prefix, boolean ignoreCase) {
        if (null == str || null == prefix) {
            return null == str && null == prefix;
        }

        if (ignoreCase) {
            return str.toString().toLowerCase().startsWith(prefix.toString().toLowerCase());
        } else {
            return str.toString().startsWith(prefix.toString());
        }
    }

    /**
     * 对字符串中${}标识的变量进行替换<br>
     * 例如
     * StringUtils2.substitute("hello ${animal} ,i am ${target}.", ImmutableMap.of("animal","monkey","target","nobody"))
     * 输出 hello monkey ,i am nobody.
     * @param content
     * @param map
     * @return
     */
    public static String substitute(String content, Map<String, ?> map) {
        StringSubstitutor sub = new StringSubstitutor(map);
        return sub.replace(content);
    }


}
