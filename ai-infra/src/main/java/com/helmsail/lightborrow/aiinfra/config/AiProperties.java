package com.helmsail.lightborrow.aiinfra.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * 共享 {@code base-url} 和 {@code api-key} 可放在 {@code ai} 根层级，
 * LLM 和 Embedding 子配置未设置时自动继承。
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private String baseUrl;
    private String apiKey;

    private LlmProperties llm = new LlmProperties();
    private EmbeddingProperties embedding = new EmbeddingProperties();
    private VectorProperties vector = new VectorProperties();

    /** 根层级共享值下推到子配置，子配置已显式设置的不覆盖 */
    @PostConstruct
    public void resolveDefaults() {
        resolve(llm, this.baseUrl, this.apiKey);
        resolve(embedding, this.baseUrl, this.apiKey);
    }

    private void resolve(AbstractAiClientProperties props, String parentBaseUrl, String parentApiKey) {
        if (props.getBaseUrl() == null) {
            props.setBaseUrl(parentBaseUrl);
        }
        if (props.getApiKey() == null) {
            props.setApiKey(parentApiKey);
        }
    }

    @Getter
    @Setter
    public static class LlmProperties extends AbstractAiClientProperties {
        private String model = "deepseek-chat";
    }

    @Getter
    @Setter
    public static class EmbeddingProperties extends AbstractAiClientProperties {
        private String model = "BAAI/bge-m3";
    }

    @Getter
    @Setter
    public static abstract class AbstractAiClientProperties {
        private String baseUrl;
        private String apiKey;
        private ClientOptions options = new ClientOptions();
    }

    @Getter
    @Setter
    public static class VectorProperties {
        private int dimension = 1024;

        @Pattern(regexp = "[a-zA-Z_][a-zA-Z0-9_]*", message = "表名只能包含字母、数字和下划线")
        private String tableName = "vector_documents";

        private int hnswM = 16;
        private int hnswEfConstruction = 64;
    }

    @Getter
    @Setter
    public static class ClientOptions {
        private Duration connectTimeout = Duration.ofSeconds(30);
        private Duration readTimeout = Duration.ofSeconds(60);
        private int maxRetries = 3;
        private int maxTokens = 4096;
        private double temperature = 0.7;
    }
}
