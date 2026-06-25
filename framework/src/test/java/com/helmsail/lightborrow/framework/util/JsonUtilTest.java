package com.helmsail.lightborrow.framework.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.FrameworkException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonUtilTest {

    @BeforeAll
    static void setUp() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        JsonUtil.setObjectMapper(mapper);
    }

    @Test
    void shouldSerializeToJson() {
        String json = JsonUtil.toJson(Map.of("key", "value"));
        assertThat(json).contains("\"key\"", "\"value\"");
    }

    @Test
    void shouldDeserializeFromJson() {
        Map<String, String> result = JsonUtil.fromJson("{\"key\":\"value\"}", Map.class);
        assertThat(result).containsEntry("key", "value");
    }

    @Test
    void shouldDeserializeWithTypeReference() {
        String json = "[1, 2, 3]";
        List<Integer> result = JsonUtil.fromJson(json, new TypeReference<>() {});
        assertThat(result).containsExactly(1, 2, 3);
    }

    @Test
    void shouldThrowFrameworkExceptionOnSerializeError() {
        assertThatThrownBy(() -> JsonUtil.toJson(new Object() {
            final Object cycle = this;
        }))
                .isInstanceOf(FrameworkException.class)
                .satisfies(e -> assertThat(((FrameworkException) e).getCode()).isEqualTo(ErrorCode.JSON_SERIALIZE_FAILED.getCode()));
    }

    @Test
    void shouldThrowFrameworkExceptionOnDeserializeError() {
        assertThatThrownBy(() -> JsonUtil.fromJson("invalid json", String.class))
                .isInstanceOf(FrameworkException.class)
                .satisfies(e -> assertThat(((FrameworkException) e).getCode()).isEqualTo(ErrorCode.JSON_DESERIALIZE_FAILED.getCode()));
    }

    @Test
    void shouldGetObjectMapper() {
        assertThat(JsonUtil.getObjectMapper()).isNotNull();
    }
}
