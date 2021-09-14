package com.pit.core.json;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Map;

/**
 * @Author gy
 * @Date 2019-06-13 12:00
 * @Description
 */
@UtilityClass
public class JacksonUtils {

    /** 默认 ObjectMapper */
    private static ObjectMapper defaultObjectMapper;
    static {
        ObjectMapper objectMapper = new ObjectMapper();
        // 忽略未知属性字段
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        //大小写脱敏
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES,true);
        //允许出现特殊字符和转义符
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true) ;
        //允许出现单引号
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true) ;
        //下划线转驼峰,有坑
//        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        // 当属性值为NULL时,不参与序列化
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        defaultObjectMapper = objectMapper;
    }

    public ObjectMapper getDefaultObjectMapper(){
        return defaultObjectMapper;
    }

    public static String toJson(Object object) {
        try {
            return defaultObjectMapper.writeValueAsString(object);
        } catch (Exception e) {
            //
        }
        return null;
    }

    public static <T> T toObject(String json, Class<T> valueType) {
        try {
            return defaultObjectMapper.readValue(json, valueType);
        } catch (Exception e) {
            throw new RuntimeException("Unsupported transform! json:"+json+", class:"+valueType,e);
        }
    }

    /**
     * 解析 T<E>
     * @param json
     * @param outClazzType
     * @param innerClazzType
     * @param <T>
     * @param <E>
     * @return
     */
    public static <T,E> T toObject(String json, Class<T> outClazzType,Class<E> innerClazzType) {
        try {
            JavaType javaType = defaultObjectMapper.getTypeFactory().constructParametricType(outClazzType,innerClazzType);
            return defaultObjectMapper.readValue(json, javaType);
        } catch (Exception e) {
            throw new RuntimeException("Unsupported transform! json:"+json+", class:"+outClazzType,e);
        }
    }

    public static <T extends Map,E,F> T toMapObject(String json, Class<T> mapClassType, Class<E> keyClass, Class<F> valueClass){
        try {
            JavaType javaType = defaultObjectMapper.getTypeFactory().constructMapType(mapClassType,keyClass,valueClass);
            return defaultObjectMapper.readValue(json, javaType);
        } catch (Exception e) {
            throw new RuntimeException("Unsupported transform! json:"+json+", class:"+mapClassType,e);
        }
    }

    /**
     * 判断是否为合法的JSON字符串
     * @param input
     * @return
     */
    public static boolean isValidJSONString(String input) {
        try {
            defaultObjectMapper.readTree(input);
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    /**
     * 判断字符串是否合法，并返回一个JsonNode
     * @param input
     * @return
     */
    public static Pair<Boolean, JsonNode> checkAndGetJsonNode(String input) {
        try {
            JsonNode jsonNode = defaultObjectMapper.readTree(input);
            return ImmutablePair.of(true, jsonNode);
        } catch (IOException ex) {
            return ImmutablePair.of(false, null);
        }
    }


    public static <T> Pair<Boolean, T> checkAndGetObject(String input, Class<T> valueType) {
        try {
            T result = defaultObjectMapper.readValue(input, valueType);
            return ImmutablePair.of(true, result);
        } catch (IOException ex) {
            return ImmutablePair.of(false, null);
        }
    }

}
