package com.helmsail.lightborrow.aiinfra.llm;

import com.helmsail.lightborrow.aiinfra.model.ChatRequest;
import com.helmsail.lightborrow.aiinfra.model.ChatResponse;

/** 支持同步对话。 */
public interface ChatModel {

    ChatResponse chat(ChatRequest request);
}
