package com.helmsail.lightborrow.core.model;

import com.helmsail.lightborrow.aiinfra.model.ChatMessage;
import com.helmsail.lightborrow.memory.model.MemoryContext;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ConversationContext {

    /** 用户 ID */
    private String userId;

    /** 用户原始输入 */
    private String userInput;

    /** 重写后的输入 */
    private String rewrittenInput;

    /** 记忆上下文 */
    private MemoryContext memoryContext;

    /** 完整消息列表（含 system prompt + history + 工具调用记录） */
    private List<ChatMessage> messages = new ArrayList<>();

    /** 最终回答 */
    private String finalAnswer;

    /** 是否有反问待处理 */
    private boolean awaitingUser;

    /** 当前等待用户回答的问题 */
    private String pendingQuestion;

    /** 是否需要用户确认 */
    private boolean awaitingConfirm;

    /** 待确认的内容 */
    private String pendingConfirmSummary;

    public ConversationContext(String userId, String userInput) {
        this.userId = userId;
        this.userInput = userInput;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
    }
}
