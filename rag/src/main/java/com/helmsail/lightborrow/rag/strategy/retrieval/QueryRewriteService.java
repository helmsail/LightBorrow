package com.helmsail.lightborrow.rag.strategy.retrieval;

import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.model.ChatMessage;
import com.helmsail.lightborrow.aiinfra.model.ChatRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 查询重写服务。将用户口语化查询重写为检索友好的形式。
 *
 * <p>例如："那个规定是啥" → "IT 资产使用管理规定"
 */
@Slf4j
public class QueryRewriteService {

    private static final String REWRITE_PROMPT = """
            你是一个查询优化助手。将用户的口语化问题重写为更适合知识库检索的形式。
            要求：
            1. 保留原始语义，补充上下文，消除指代
            2. 输出简洁的检索关键词，不要多余的解释
            3. 如果用户输入已经清晰完整，保持原样输出
            4. 只输出重写后的文本，不要任何前缀
            """;

    private final ChatModel chatModel;

    public QueryRewriteService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 重写用户查询。
     *
     * @param query 用户原始查询
     * @return 重写后的查询
     */
    public String rewrite(String query) {
        if (query == null || query.isBlank()) return query;

        ChatRequest request = ChatRequest.builder()
                .model(null)
                .messages(List.of(
                        ChatMessage.system(REWRITE_PROMPT),
                        ChatMessage.user(query)))
                .temperature(0.1)
                .maxTokens(256)
                .stream(false)
                .build();

        try {
            var response = chatModel.chat(request);
            if (response != null && response.content() != null && !response.content().isBlank()) {
                String rewritten = response.content().trim();
                log.debug("[RAG] 查询重写: '{}' -> '{}'", query, rewritten);
                return rewritten;
            }
        } catch (Exception e) {
            log.warn("[RAG] 查询重写失败，使用原始查询", e);
        }
        return query;
    }
}
