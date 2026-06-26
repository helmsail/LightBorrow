package com.helmsail.lightborrow.core.agent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentResultTest {

    @Test
    void shouldCreateFinalAnswer() {
        AgentResult result = AgentResult.finalAnswer("已完成借用申请");
        assertThat(result.getType()).isEqualTo("final");
        assertThat(result.getContent()).isEqualTo("已完成借用申请");
        assertThat(result.isFinal()).isTrue();
        assertThat(result.isQuestion()).isFalse();
        assertThat(result.isConfirm()).isFalse();
    }

    @Test
    void shouldCreateQuestion() {
        AgentResult result = AgentResult.question("请问您想借用哪台设备？");
        assertThat(result.getType()).isEqualTo("question");
        assertThat(result.getContent()).isEqualTo("请问您想借用哪台设备？");
        assertThat(result.isFinal()).isFalse();
        assertThat(result.isQuestion()).isTrue();
        assertThat(result.isConfirm()).isFalse();
    }

    @Test
    void shouldCreateConfirm() {
        AgentResult result = AgentResult.confirm("确认借用MBP-2024-001？");
        assertThat(result.getType()).isEqualTo("confirm");
        assertThat(result.getContent()).isEqualTo("确认借用MBP-2024-001？");
        assertThat(result.isFinal()).isFalse();
        assertThat(result.isQuestion()).isFalse();
        assertThat(result.isConfirm()).isTrue();
    }

    @Test
    void shouldCreateError() {
        AgentResult result = AgentResult.error("系统繁忙，请稍后重试");
        assertThat(result.getType()).isEqualTo("error");
        assertThat(result.getContent()).isEqualTo("系统繁忙，请稍后重试");
        assertThat(result.isFinal()).isFalse();
        assertThat(result.isQuestion()).isFalse();
        assertThat(result.isConfirm()).isFalse();
    }

    @Test
    void shouldHandleEmptyContent() {
        AgentResult result = AgentResult.finalAnswer("");
        assertThat(result.getContent()).isEmpty();
        assertThat(result.isFinal()).isTrue();
    }

    @Test
    void shouldHandleNullContent() {
        AgentResult result = AgentResult.finalAnswer(null);
        assertThat(result.getContent()).isNull();
        assertThat(result.isFinal()).isTrue();
    }
}
