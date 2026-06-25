package com.helmsail.lightborrow.core.rewrite;

import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.model.ChatMessage;
import com.helmsail.lightborrow.aiinfra.model.ChatRequest;
import com.helmsail.lightborrow.core.model.ConversationContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class LlmRewriteStage implements RewriteStage {

    private static final String SYSTEM_PROMPT = """
            你是一个对话输入重写助手。根据对话历史，将用户的最新输入重写为一个完整、独立的问题/请求，消除指代歧义。
            直接输出重写后的文本，不要任何前缀、引号或解释。如果输入已完整且无歧义，保持原样输出。
            """;

    private final ChatModel chatModel;

    public LlmRewriteStage(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public void rewrite(ConversationContext ctx) {
        String input = ctx.getRewrittenInput() != null
                ? ctx.getRewrittenInput() : ctx.getUserInput();

        // 如果历史对话为空，无需重写
        if (ctx.getMemoryContext() == null
                || ctx.getMemoryContext().getHistoryMessages() == null
                || ctx.getMemoryContext().getHistoryMessages().isEmpty()) {
            log.debug("[Core] LlmRewriteStage: 无历史对话，跳过重写");
            return;
        }

        // 构建历史上下文
        StringBuilder history = new StringBuilder("## 对话历史\n");
        for (String msg : ctx.getMemoryContext().getHistoryMessages()) {
            history.append(msg).append("\n");
        }

        ChatRequest request = ChatRequest.builder()
                .model(null)
                .messages(List.of(
                        ChatMessage.system(SYSTEM_PROMPT + "\n\n" + history),
                        ChatMessage.user("请重写：" + input)))
                .temperature(0.1)
                .maxTokens(256)
                .stream(false)
                .build();

        try {
            var response = chatModel.chat(request);
            if (response != null && response.content() != null
                    && !response.content().isBlank()) {
                String rewritten = response.content().trim();
                ctx.setRewrittenInput(rewritten);
                log.debug("[Core] LLM 重写: '{}' -> '{}'", input, rewritten);
            }
        } catch (Exception e) {
            log.warn("[Core] LLM 重写失败，使用原始输入: {}", input, e);
        }
    }
}
