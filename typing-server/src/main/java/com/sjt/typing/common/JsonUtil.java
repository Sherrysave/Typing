package com.sjt.typing.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * JSON 工具类：包装一个静态 ObjectMapper，供实体类的手写 JSON 字符串列与 DTO 之间互转。
 * 所有异常统一转为 BusinessException(BIZ_ERROR)，避免调用方到处处理受检异常。
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    private static final ObjectMapper MAPPER = new ObjectMapper()
            // 遇到未知字段不报错，便于前后端字段演进
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /** 对象序列化为 JSON 字符串 */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.BIZ_ERROR, "JSON 序列化失败: " + e.getMessage());
        }
    }

    /** JSON 字符串反序列化为指定类型 */
    public static <T> T parse(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.BIZ_ERROR, "JSON 解析失败: " + e.getMessage());
        }
    }

    /** JSON 字符串反序列化为 List<T> */
    public static <T> List<T> parseList(String json, Class<T> elemType) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, elemType));
        } catch (Exception e) {
            throw new BusinessException(ResultCode.BIZ_ERROR, "JSON 解析失败: " + e.getMessage());
        }
    }

    /** JSON 字符串反序列化为任意泛型（如 List<Map<String,Object>>） */
    public static <T> T parse(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.BIZ_ERROR, "JSON 解析失败: " + e.getMessage());
        }
    }
}
