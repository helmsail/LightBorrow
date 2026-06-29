package com.helmsail.lightborrow.core.rewrite;

import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.model.ChatMessage;
import com.helmsail.lightborrow.aiinfra.model.ChatRequest;
import com.helmsail.lightborrow.core.model.ConversationContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class LlmRewriteStage implements RewriteStage {

    private static final String REWRITE_PROMPT_PATH = "prompts/rewrite-prompt.md";

    private final ChatModel chatModel;
    private final PromptTemplateService promptTemplateService;

    public LlmRewriteStage(ChatModel chatModel, PromptTemplateService promptTemplateService) {
        this.chatModel = chatModel;
        this.promptTemplateService = promptTemplateService;
    }

    @Override
    public void rewrite(ConversationContext ctx) {
        // 如果 RuleRewriteStage 判定不需要 LLM 重写，直接跳过
        if (!ctx.isRequireRewrite()) {
            log.debug("[Core] LlmRewriteStage: 规则判定跳过重写");
            return;
        }

        String input = ctx.getRewrittenInput() != null
                ? ctx.getRewrittenInput() : ctx.getUserInput();

        if (ctx.getMemoryContext() == null
                || ctx.getMemoryContext().getHistoryMessages() == null
                || ctx.getMemoryContext().getHistoryMessages().isEmpty()) {
            log.debug("[Core] LlmRewriteStage: 无历史对话，跳过重写");
            return;
        }

        StringBuilder history = new StringBuilder("## 对话历史\n");
        for (String msg : ctx.getMemoryContext().getHistoryMessages()) {
            history.append(msg).append("\n");
        }

        String systemPrompt = promptTemplateService.getRaw(REWRITE_PROMPT_PATH)
                + "\n\n" + history;

        ChatRequest request = ChatRequest.builder()
                .model(null)
                .messages(List.of(
                        ChatMessage.system(systemPrompt),
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
