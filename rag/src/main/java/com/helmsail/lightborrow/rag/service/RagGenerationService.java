package com.helmsail.lightborrow.rag.service;

import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.model.ChatMessage;
import com.helmsail.lightborrow.aiinfra.model.ChatRequest;
import com.helmsail.lightborrow.rag.model.DocumentChunk;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RagGenerationService {

    private final ChatModel chatModel;

    public RagGenerationService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 基于检索结果生成回答。
     *
     * @param query               用户原始查询
     * @param chunks              检索到的相关文档块
     * @param citedContext        带引文的上下文
     * @param citationInstruction 引文要求
     * @return 生成的回答
     */
    public String generate(String query, List<DocumentChunk> chunks,
                           String citedContext, String citationInstruction) {
        if (chunks == null || chunks.isEmpty()) {
            return "未找到相关知识。";
        }

        String systemPrompt = String.format("""
                你是一个知识库助手。请基于以下提供的参考资料回答用户的问题。
                如果参考资料不足以回答，请如实告知。
                请用中文回答，保持简洁准确。

                参考资料：
                %s

                %s""", citedContext, citationInstruction);

        ChatRequest request = ChatRequest.builder()
                .model(null)
                .messages(List.of(
                        ChatMessage.system(systemPrompt),
                        ChatMessage.user(query)))
                .temperature(0.3)
                .maxTokens(1024)
                .stream(false)
                .build();

        var response = chatModel.chat(request);
        String answer = response != null ? response.content() : "生成回答失败。";
        log.debug("[RAG] 生成完成, query={}, chunks={}", query, chunks.size());
        return answer;
    }
}
