package com.helmsail.lightborrow.rag.config;

import com.helmsail.lightborrow.aiinfra.embedding.EmbeddingModel;
import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.vector.VectorStore;
import com.helmsail.lightborrow.rag.pipeline.offline.RagOfflinePipeline;
import com.helmsail.lightborrow.rag.pipeline.online.RagOnlinePipeline;
import com.helmsail.lightborrow.rag.service.RagGenerationService;
import com.helmsail.lightborrow.rag.strategy.chunking.ChunkingStrategy;
import com.helmsail.lightborrow.rag.strategy.chunking.FixedSizeChunker;
import com.helmsail.lightborrow.rag.strategy.retriever.RetrievalStrategy;
import com.helmsail.lightborrow.rag.strategy.retriever.SimilarityRetriever;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(RagProperties.class)
public class RagAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ChunkingStrategy chunkingStrategy(RagProperties ragProperties) {
        return new FixedSizeChunker(ragProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public RetrievalStrategy retrievalStrategy(RagProperties ragProperties) {
        return new SimilarityRetriever(ragProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ChatModel.class)
    public RagGenerationService ragGenerationService(ChatModel chatModel) {
        return new RagGenerationService(chatModel);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({EmbeddingModel.class, VectorStore.class, ChatModel.class})
    public RagOnlinePipeline ragOnlinePipeline(EmbeddingModel embeddingModel,
                                               VectorStore vectorStore,
                                               RagGenerationService generationService) {
        return new RagOnlinePipeline(embeddingModel, vectorStore, generationService);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({EmbeddingModel.class, VectorStore.class})
    public RagOfflinePipeline ragOfflinePipeline(ChunkingStrategy chunkingStrategy,
                                                 EmbeddingModel embeddingModel,
                                                 VectorStore vectorStore) {
        return new RagOfflinePipeline(chunkingStrategy, embeddingModel, vectorStore);
    }
}
