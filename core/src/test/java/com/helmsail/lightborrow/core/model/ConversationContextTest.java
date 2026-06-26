package com.helmsail.lightborrow.core.model;

import com.helmsail.lightborrow.aiinfra.model.ChatMessage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationContextTest {

    @Test
    void shouldCreateWithUserIdAndUserInput() {
        ConversationContext ctx = new ConversationContext("user123", "你好");

        assertThat(ctx.getUserId()).isEqualTo("user123");
        assertThat(ctx.getUserInput()).isEqualTo("你好");
    }

    @Test
    void shouldAddMessage() {
        ConversationContext ctx = new ConversationContext("user123", "hello");
        ChatMessage msg = ChatMessage.assistant("Hi there");

        ctx.addMessage(msg);

        assertThat(ctx.getMessages()).hasSize(1);
        assertThat(ctx.getMessages().get(0).content()).isEqualTo("Hi there");
    }

    @Test
    void shouldInitializeWithEmptyMessages() {
        ConversationContext ctx = new ConversationContext("user1", "test");

        assertThat(ctx.getMessages()).isEmpty();
    }

    @Test
    void shouldSetAndGetAllFields() {
        ConversationContext ctx = new ConversationContext("user1", "input");
        ctx.setRewrittenInput("rewritten");
        ctx.setFinalAnswer("answer");
        ctx.setAwaitingUser(true);
        ctx.setPendingQuestion("你确定吗？");
        ctx.setAwaitingConfirm(true);
        ctx.setPendingConfirmSummary("确认内容");

        assertThat(ctx.getRewrittenInput()).isEqualTo("rewritten");
        assertThat(ctx.getFinalAnswer()).isEqualTo("answer");
        assertThat(ctx.isAwaitingUser()).isTrue();
        assertThat(ctx.getPendingQuestion()).isEqualTo("你确定吗？");
        assertThat(ctx.isAwaitingConfirm()).isTrue();
        assertThat(ctx.getPendingConfirmSummary()).isEqualTo("确认内容");
    }
}
