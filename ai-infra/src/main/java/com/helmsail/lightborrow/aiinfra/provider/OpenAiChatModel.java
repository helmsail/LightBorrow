package com.helmsail.lightborrow.aiinfra.provider;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.helmsail.lightborrow.aiinfra.config.AiProperties;
import com.helmsail.lightborrow.aiinfra.exception.AiException;
import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.model.ChatRequest;
import com.helmsail.lightborrow.aiinfra.model.ChatResponse;
import com.helmsail.lightborrow.framework.sentinel.SentinelAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

import static com.helmsail.lightborrow.framework.constant.ErrorCode.AI_API_CALL_FAILED;

/**
 * OpenAI 兼容实现（如 DeepSeek）。
 * 使用 RestClient + Sentinel 熔断。
 */
@Slf4j
public class OpenAiChatModel implements ChatModel {

    private static final String CHAT_PATH = "/chat/completions";

    private final RestClient restClient;
    private final AiProperties.LlmProperties properties;

    public OpenAiChatModel(RestClient restClient,
                           AiProperties.LlmProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        ChatRequest finalRequest = withDefaults(request, false);
        try (Entry entry = SphU.entry(SentinelAutoConfiguration.RESOURCE_LLM_CHAT)) {
            ChatResponse response = doChat(finalRequest);
            return response;
        } catch (AiException e) {
            throw e;
        } catch (Exception e) {
            throw new AiException(AI_API_CALL_FAILED, e, properties.getModel());
        }
    }

    private ChatResponse doChat(ChatRequest request) {
        log.debug("Calling LLM chat: model={}", request.model());
        ChatResponse response = restClient.post()
                .uri(CHAT_PATH)
                .body(request)
                .retrieve()
                .body(ChatResponse.class);
        if (response == null) {
            throw new AiException(AI_API_CALL_FAILED, "LLM returned null response");
        }
        return response;
    }

    private ChatRequest withDefaults(ChatRequest original, boolean stream) {
        AiProperties.ClientOptions opts = properties.getOptions();
        return ChatRequest.builder()
                .model(original.model() != null ? original.model() : properties.getModel())
                .messages(original.messages())
                .temperature(original.temperature() != null ? original.temperature() : opts.getTemperature())
                .maxTokens(original.maxTokens() != null ? original.maxTokens() : opts.getMaxTokens())
                .stream(stream)
                .tools(original.tools())
                .build();
    }
}
