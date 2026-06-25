package com.helmsail.lightborrow.aiinfra.llm;

import com.helmsail.lightborrow.aiinfra.model.ChatRequest;
import com.helmsail.lightborrow.aiinfra.model.ChatResponse;
import com.helmsail.lightborrow.aiinfra.model.ChatResponseChunk;
import reactor.core.publisher.Flux;

/**
 * 大语言模型调用接口。支持同步对话和流式对话。
 */
public interface ChatModel {

    ChatResponse chat(ChatRequest request);

    Flux<ChatResponseChunk> stream(ChatRequest request);
}
