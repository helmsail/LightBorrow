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

/**
 * Jackson JSON 工具封装。持有独立 ObjectMapper 兜底，Spring 启动后注入已配置的 ObjectMapper。
 */
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

    @SuppressWarnings("unchecked")
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

    private static ObjectMapper createDefaultMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
