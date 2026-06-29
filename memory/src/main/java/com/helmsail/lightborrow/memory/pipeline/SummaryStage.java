package com.helmsail.lightborrow.memory.pipeline;

import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.model.ChatMessage;
import com.helmsail.lightborrow.aiinfra.model.ChatRequest;
import com.helmsail.lightborrow.memory.config.MemoryProperties;
import com.helmsail.lightborrow.memory.model.MemoryContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话摘要阶段。当历史消息超出 maxHistory 时，用 LLM 摘要旧历史。
 *
 * <p>在 load() 阶段检查，将最早的 N 条消息摘要为几句话，
 * 替换被摘要的消息，节省 Token。
 */
@Slf4j
public class SummaryStage implements MemoryStage {

    private static final int SUMMARY_BATCH = 10;

    private final ChatModel chatModel;
    private final int maxHistory;

    public SummaryStage(ChatModel chatModel, MemoryProperties properties) {
        this.chatModel = chatModel;
        this.maxHistory = properties.getMaxHistory();
    }

    @Override
    public void load(MemoryContext ctx) {
        List<String> messages = ctx.getHistoryMessages();
        if (messages == null || messages.size() <= maxHistory) {
            return;
        }

        // 提取最早的消息进行摘要
        List<String> toSummarize = messages.subList(0, Math.min(SUMMARY_BATCH, messages.size() - maxHistory / 2));
        String summary = summarizeMessages(toSummarize);

        if (summary != null) {
            List<String> remaining = new ArrayList<>(messages.subList(toSummarize.size(), messages.size()));
            remaining.add(0, "[历史摘要] " + summary);
            ctx.setHistoryMessages(remaining);
            log.info("[Memory] 对话摘要完成: {} 条 -> 1 条摘要", toSummarize.size());
        }
    }

    @Override
    public void save(MemoryContext ctx) {
        // 摘要无特殊保存逻辑
    }

    private String summarizeMessages(List<String> messages) {
        try {
            StringBuilder sb = new StringBuilder();
            for (String msg : messages) {
                sb.append(msg).append("\n");
            }

            ChatRequest request = ChatRequest.builder()
                    .model(null)
                    .messages(List.of(
                            ChatMessage.system("""
                                    你是一个对话摘要助手。请将以下对话内容概括为 3-5 个句子，
                                    保留关键信息和上下文。只输出摘要，不要任何前缀。"""),
                            ChatMessage.user(sb.toString())))
                    .temperature(0.3)
                    .maxTokens(512)
                    .stream(false)
                    .build();

            var response = chatModel.chat(request);
            if (response != null && response.content() != null) {
                return response.content().trim();
            }
        } catch (Exception e) {
            log.warn("[Memory] 摘要生成失败", e);
        }
        return null;
    }
}
