package com.helmsail.lightborrow.core.agent;

import lombok.Getter;

@Getter
public class AgentResult {

    private final AgentResultType type;
    private final String content;

    private AgentResult(AgentResultType type, String content) {
        this.type = type;
        this.content = content;
    }

    public static AgentResult finalAnswer(String content) {
        return new AgentResult(AgentResultType.FINAL_ANSWER, content);
    }

    public static AgentResult question(String content) {
        return new AgentResult(AgentResultType.QUESTION, content);
    }

    public static AgentResult confirm(String content) {
        return new AgentResult(AgentResultType.CONFIRM, content);
    }

    public static AgentResult error(String content) {
        return new AgentResult(AgentResultType.ERROR, content);
    }
}
