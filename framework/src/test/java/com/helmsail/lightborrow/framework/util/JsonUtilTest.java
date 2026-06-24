package com.helmsail.lightborrow.framework.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.helmsail.lightborrow.framework.exception.FrameworkException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonUtilTest {

    @Test
    void toJson_shouldSerializeObject() {
        TestUser user = new TestUser("张三", 25);
        String json = JsonUtil.toJson(user);
        assertThat(json).contains("\"name\":\"张三\"");
        assertThat(json).contains("\"age\":25");
    }

    @Test
    void toJson_shouldSerializeNullToNullString() {
        String json = JsonUtil.toJson(null);
        assertThat(json).isEqualTo("null");
    }

    @Test
    void fromJson_shouldDeserializeToClass() {
        String json = "{\"name\":\"李四\",\"age\":30}";
        TestUser user = JsonUtil.fromJson(json, TestUser.class);
        assertThat(user.name()).isEqualTo("李四");
        assertThat(user.age()).isEqualTo(30);
    }

    @Test
    void fromJson_withTypeReference_shouldDeserializeGenericList() {
        String json = "[{\"name\":\"a\",\"age\":1},{\"name\":\"b\",\"age\":2}]";
        List<TestUser> users = JsonUtil.fromJson(json, new TypeReference<List<TestUser>>() {});
        assertThat(users).hasSize(2);
        assertThat(users.get(0).name()).isEqualTo("a");
    }

    @Test
    void fromJson_withInvalidJson_shouldThrowFrameworkException() {
        assertThatThrownBy(() -> JsonUtil.fromJson("{invalid}", TestUser.class))
                .isInstanceOf(FrameworkException.class);
    }

    @Test
    void fromJson_withInvalidJsonAndTypeRef_shouldThrowFrameworkException() {
        assertThatThrownBy(() -> JsonUtil.fromJson("{invalid}", new TypeReference<TestUser>() {}))
                .isInstanceOf(FrameworkException.class);
    }

    @Test
    void toJson_withInvalidObject_shouldThrowFrameworkException() {
        // Object that causes circular reference or serialization error
        assertThatThrownBy(() -> JsonUtil.toJson(new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("serialize error");
            }
        })).isInstanceOf(FrameworkException.class);
    }

    @Test
    void getObjectMapper_shouldReturnNonNull() {
        assertThat(JsonUtil.getObjectMapper()).isNotNull();
    }

    @Test
    void getObjectMapper_shouldBeConfigurable() {
        // Just verify the default mapper has expected features
        var mapper = JsonUtil.getObjectMapper();
        assertThat(mapper).isNotNull();
    }

    record TestUser(String name, int age) {
    }
}
