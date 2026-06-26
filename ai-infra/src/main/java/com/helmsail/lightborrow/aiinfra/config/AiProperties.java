package com.helmsail.lightborrow.aiinfra.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * AI 基础设施配置属性。
 *
 * <p>共享 {@code base-url} 和 {@code api-key} 可放在 {@code ai} 根层级，
 * LLM 和 Embedding 子配置未设置时自动继承。也支持子配置独立覆盖。
 *
 * <pre>
 * ai:
 *   base-url: https://api.deepseek.com           # 共享默认值
 *   api-key: ${DEEPSEEK_API_KEY}                 # 共享默认值
 *   llm:
 *     model: deepseek-chat                       # 只需指定差异项
 *   embedding:
 *     model: BAAI/bge-m3                           # 只需指定差异项
 *   vector:
 *     dimension: 1024
 *     table-name: vector_documents
 *     hnsw-m: 16
 *     hnsw-ef-construction: 64
 * </pre>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** 共享 API Base URL（LLM/Embedding 未设置时自动继承） */
    private String baseUrl;

    /** 共享 API Key（LLM/Embedding 未设置时自动继承） */
    private String apiKey;

    private LlmProperties llm = new LlmProperties();
    private EmbeddingProperties embedding = new EmbeddingProperties();
    private VectorProperties vector = new VectorProperties();

    /**
     * 将根层级共享值下推到子配置。子配置已显式设置的字段不会被覆盖。
     */
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
