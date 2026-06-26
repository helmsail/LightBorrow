package com.helmsail.lightborrow.memory.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryContextTest {

    @Test
    void builderShouldCreateWithAllFields() {
        SessionState session = SessionState.builder().userId("user1").build();
        MemoryContext ctx = MemoryContext.builder()
                .userId("user1")
                .sessionState(session)
                .historyMessages(List.of("msg1", "msg2"))
                .profileSummary("技术型用户")
                .newSession(true)
                .build();

        assertThat(ctx.getUserId()).isEqualTo("user1");
        assertThat(ctx.getSessionState()).isSameAs(session);
        assertThat(ctx.getHistoryMessages()).containsExactly("msg1", "msg2");
        assertThat(ctx.getProfileSummary()).isEqualTo("技术型用户");
        assertThat(ctx.isNewSession()).isTrue();
    }

    @Test
    void builderShouldProvideDefaults() {
        MemoryContext ctx = MemoryContext.builder()
                .userId("user2")
                .build();

        assertThat(ctx.getUserId()).isEqualTo("user2");
        assertThat(ctx.getSessionState()).isNull();
        assertThat(ctx.getHistoryMessages()).isNull();
        assertThat(ctx.getProfileSummary()).isNull();
        assertThat(ctx.isNewSession()).isFalse();
    }

    @Test
    void noArgsConstructorShouldCreateEmpty() {
        MemoryContext ctx = new MemoryContext();

        assertThat(ctx.getUserId()).isNull();
        assertThat(ctx.isNewSession()).isFalse();
    }

    @Test
    void allArgsConstructorShouldSetFields() {
        SessionState session = SessionState.builder().build();
        MemoryContext ctx = new MemoryContext("user3", session, List.of("hi"), "profile", true);

        assertThat(ctx.getUserId()).isEqualTo("user3");
        assertThat(ctx.getSessionState()).isSameAs(session);
        assertThat(ctx.getHistoryMessages()).containsExactly("hi");
        assertThat(ctx.getProfileSummary()).isEqualTo("profile");
        assertThat(ctx.isNewSession()).isTrue();
    }

    @Test
    void settersShouldUpdateFields() {
        MemoryContext ctx = new MemoryContext();
        ctx.setUserId("user4");
        ctx.setNewSession(true);

        assertThat(ctx.getUserId()).isEqualTo("user4");
        assertThat(ctx.isNewSession()).isTrue();
    }
}
