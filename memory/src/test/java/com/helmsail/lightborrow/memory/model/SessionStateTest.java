package com.helmsail.lightborrow.memory.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionStateTest {

    @Test
    void builderShouldCreateWithAllFields() {
        SessionState state = SessionState.builder()
                .userId("user1")
                .intent("查询余额")
                .status("ACTIVE")
                .createdAt(1000L)
                .lastAccessAt(2000L)
                .build();

        assertThat(state.getUserId()).isEqualTo("user1");
        assertThat(state.getIntent()).isEqualTo("查询余额");
        assertThat(state.getStatus()).isEqualTo("ACTIVE");
        assertThat(state.getCreatedAt()).isEqualTo(1000L);
        assertThat(state.getLastAccessAt()).isEqualTo(2000L);
    }

    @Test
    void builderShouldProvideDefaults() {
        SessionState state = SessionState.builder()
                .userId("user2")
                .build();

        assertThat(state.getUserId()).isEqualTo("user2");
        assertThat(state.getIntent()).isNull();
        assertThat(state.getStatus()).isNull();
        assertThat(state.getCreatedAt()).isZero();
        assertThat(state.getLastAccessAt()).isZero();
    }

    @Test
    void noArgsConstructorShouldCreateEmpty() {
        SessionState state = new SessionState();

        assertThat(state.getUserId()).isNull();
        assertThat(state.getCreatedAt()).isZero();
    }

    @Test
    void allArgsConstructorShouldSetFields() {
        SessionState state = new SessionState("user3", "转账", "COMPLETED", 3000L, 4000L);

        assertThat(state.getUserId()).isEqualTo("user3");
        assertThat(state.getIntent()).isEqualTo("转账");
        assertThat(state.getStatus()).isEqualTo("COMPLETED");
        assertThat(state.getCreatedAt()).isEqualTo(3000L);
        assertThat(state.getLastAccessAt()).isEqualTo(4000L);
    }

    @Test
    void settersShouldUpdateFields() {
        SessionState state = new SessionState();
        state.setUserId("user4");
        state.setStatus("ACTIVE");

        assertThat(state.getUserId()).isEqualTo("user4");
        assertThat(state.getStatus()).isEqualTo("ACTIVE");
    }
}
