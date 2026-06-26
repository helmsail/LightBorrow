package com.helmsail.lightborrow.core.rewrite;

import com.helmsail.lightborrow.core.model.ConversationContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CleanRewriteStageTest {

    private final CleanRewriteStage stage = new CleanRewriteStage();

    @Test
    void shouldTrimLeadingAndTrailingWhitespace() {
        ConversationContext ctx = new ConversationContext("user1", "  帮我查一下   ");
        stage.rewrite(ctx);
        assertThat(ctx.getRewrittenInput()).isEqualTo("帮我查一下");
    }

    @Test
    void shouldCollapseMultipleSpacesToSingleSpace() {
        ConversationContext ctx = new ConversationContext("user1", "帮我    查   一下");
        stage.rewrite(ctx);
        assertThat(ctx.getRewrittenInput()).isEqualTo("帮我 查 一下");
    }

    @Test
    void shouldHandleNullInput() {
        ConversationContext ctx = new ConversationContext("user1", null);
        stage.rewrite(ctx);
        assertThat(ctx.getRewrittenInput()).isNull();
    }

    @Test
    void shouldHandleBlankInput() {
        ConversationContext ctx = new ConversationContext("user1", "   ");
        stage.rewrite(ctx);
        assertThat(ctx.getRewrittenInput()).isNull();
    }

    @Test
    void shouldHandleEmptyInput() {
        ConversationContext ctx = new ConversationContext("user1", "");
        stage.rewrite(ctx);
        assertThat(ctx.getRewrittenInput()).isNull();
    }

    @Test
    void shouldHandleAlreadyCleanInput() {
        ConversationContext ctx = new ConversationContext("user1", "帮我查一下电脑");
        stage.rewrite(ctx);
        assertThat(ctx.getRewrittenInput()).isEqualTo("帮我查一下电脑");
    }

    @Test
    void shouldCollapseTabsAndNewlinesToSingleSpace() {
        ConversationContext ctx = new ConversationContext("user1", "帮我\n\t查一下");
        stage.rewrite(ctx);
        assertThat(ctx.getRewrittenInput()).isEqualTo("帮我 查一下");
    }

    @Test
    void shouldHandleMixedWhitespace() {
        ConversationContext ctx = new ConversationContext("user1", "  帮我  查  \n  一下  ");
        stage.rewrite(ctx);
        assertThat(ctx.getRewrittenInput()).isEqualTo("帮我 查 一下");
    }
}
