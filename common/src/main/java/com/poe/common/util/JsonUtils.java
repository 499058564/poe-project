package com.poe.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * 基于 Jackson 的 JSON 序列化/反序列化工具类。
 * 所有方法在失败时抛出 {@link RuntimeException}。
 */
public final class JsonUtils {

    /** 共享的线程安全 {@link ObjectMapper} 实例。 */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {}

    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param obj 待序列化的对象
     * @return JSON 字符串
     * @throws RuntimeException 序列化失败时抛出
     */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型的对象。
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return 反序列化后的对象
     * @throws RuntimeException 反序列化失败时抛出
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to " + clazz.getName(), e);
        }
    }

    /**
     * 将 JSON 数组字符串反序列化为指定元素类型的 {@link List}。
     *
     * @param json  JSON 数组字符串
     * @param clazz 元素类型
     * @param <T>   元素类型
     * @return 反序列化后的列表
     * @throws RuntimeException 反序列化失败时抛出
     */
    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json,
                MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON list of " + clazz.getName(), e);
        }
    }
}
