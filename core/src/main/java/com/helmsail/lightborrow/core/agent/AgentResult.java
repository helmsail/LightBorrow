package com.helmsail.lightborrow.core.agent;

import lombok.Getter;

@Getter
public class AgentResult {

    /** 消息类型：final / question / confirm / error */
    private final String type;
    private final String content;

    private AgentResult(String type, String content) {
        this.type = type;
        this.content = content;
    }

    public static AgentResult finalAnswer(String content) {
        return new AgentResult("final", content);
    }

    public static AgentResult question(String content) {
        return new AgentResult("question", content);
    }

    public static AgentResult confirm(String content) {
        return new AgentResult("confirm", content);
    }

    public static AgentResult error(String content) {
        return new AgentResult("error", content);
    }

    public boolean isFinal() {
        return "final".equals(type);
    }

    public boolean isQuestion() {
        return "question".equals(type);
    }

    public boolean isConfirm() {
        return "confirm".equals(type);
    }
}
