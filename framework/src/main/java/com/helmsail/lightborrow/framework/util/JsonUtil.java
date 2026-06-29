package com.helmsail.lightborrow.framework.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.FrameworkException;
import lombok.extern.slf4j.Slf4j;

/** Spring 启动后注入已配置的 ObjectMapper。 */
@Slf4j
public final class JsonUtil {

    private static volatile ObjectMapper objectMapper = createDefaultMapper();

    private JsonUtil() {
    }

    /** 注入 Spring 管理的 ObjectMapper */
    public static void setObjectMapper(ObjectMapper mapper) {
        objectMapper = mapper;
    }

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("[JSON序列化] 失败", e);
            throw new FrameworkException(ErrorCode.JSON_SERIALIZE_FAILED, e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("[JSON反序列化] 失败 json={}", json, e);
            throw new FrameworkException(ErrorCode.JSON_DESERIALIZE_FAILED, e);
        }
    }

    @SuppressWarnings("unchecked") // TypeReference 泛型擦除，类型安全由调用方保证
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error("[JSON反序列化] 失败 json={}", json, e);
            throw new FrameworkException(ErrorCode.JSON_DESERIALIZE_FAILED, e);
        }
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * 将 float[] 转换为 pgvector 字符串格式，如 '[0.1,0.2,0.3]'。
     */
    public static String toPgVector(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(embedding[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    private static ObjectMapper createDefaultMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
