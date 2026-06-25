package com.helmsail.lightborrow.aiinfra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * AI 基础设施配置属性。
 * <pre>
 * ai:
 *   llm:
 *     base-url: https://api.deepseek.com
 *     api-key: ${DEEPSEEK_API_KEY}
 *     model: deepseek-chat
 *     options:
 *       connect-timeout: 30s
 *       read-timeout: 60s
 *       max-tokens: 4096
 *       temperature: 0.7
 *   embedding:
 *     base-url: https://api.deepseek.com
 *     api-key: ${DEEPSEEK_API_KEY}
 *     model: deepseek-embedding
 *     options:
 *       connect-timeout: 30s
 *       read-timeout: 60s
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

    private LlmProperties llm = new LlmProperties();
    private EmbeddingProperties embedding = new EmbeddingProperties();
    private VectorProperties vector = new VectorProperties();

    @Getter
    @Setter
    public static class LlmProperties {
        private String baseUrl;
        private String apiKey;
        private String model = "deepseek-chat";
        private ClientOptions options = new ClientOptions();
    }

    @Getter
    @Setter
    public static class EmbeddingProperties {
        private String baseUrl;
        private String apiKey;
        private String model = "deepseek-embedding";
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
