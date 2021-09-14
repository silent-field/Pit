package com.pit.core.json;

import com.google.common.base.Charsets;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.FormatFactory;
import com.googlecode.protobuf.format.ProtobufFormatter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author gy
 * Protobuf 对象转 json 工具类
 */
public class ProtobufJsonUtils {
    private static final FormatFactory formatFactory = new FormatFactory();

    private static final ProtobufFormatter formatter = formatFactory.createFormatter(FormatFactory.Formatter.JSON);

    static {
        formatter.setDefaultCharset(Charsets.UTF_8);
    }

    private ProtobufJsonUtils() {
    }

    public static ProtobufFormatter getJsonFormat() {
        return formatter;
    }

    public static String printToString(Message message) {
        if (null == message) {
            return StringUtils.EMPTY;
        }
        return formatter.printToString(message);
    }
}