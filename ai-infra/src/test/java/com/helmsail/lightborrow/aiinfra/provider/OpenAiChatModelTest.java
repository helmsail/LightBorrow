package com.helmsail.lightborrow.aiinfra.provider;

import com.helmsail.lightborrow.aiinfra.config.AiProperties;
import com.helmsail.lightborrow.aiinfra.model.ChatMessage;
import com.helmsail.lightborrow.aiinfra.model.ChatRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiChatModelTest {

    private final AiProperties.LlmProperties llmProperties = createDefaultProperties();

    @Test
    void shouldCreateInstance() {
        var model = new OpenAiChatModel(
                RestClient.create(),
                llmProperties);

        assertThat(model).isNotNull();
    }

    @Test
    void shouldUseDefaultsInChatRequest() {
        var model = new OpenAiChatModel(
                RestClient.create(),
                llmProperties);

        ChatRequest request = ChatRequest.builder()
                .model(null)
                .messages(List.of(ChatMessage.user("hello")))
                .temperature(null)
                .maxTokens(null)
                .stream(false)
                .build();

        assertThat(request).isNotNull();
        assertThat(request.messages()).hasSize(1);
        assertThat(request.messages().get(0).content()).isEqualTo("hello");
    }

    private static AiProperties.LlmProperties createDefaultProperties() {
        var props = new AiProperties.LlmProperties();
        props.setBaseUrl("https://api.test.com");
        props.setApiKey("test-key");
        props.setModel("test-model");
        return props;
    }
}
