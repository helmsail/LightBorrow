package com.helmsail.lightborrow.rag.config;

import com.helmsail.lightborrow.aiinfra.embedding.EmbeddingModel;
import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.mapper.VectorDocumentMapper;
import com.helmsail.lightborrow.rag.pipeline.offline.RagOfflinePipeline;
import com.helmsail.lightborrow.rag.pipeline.online.RagOnlinePipeline;
import com.helmsail.lightborrow.rag.service.RagGenerationService;
import com.helmsail.lightborrow.rag.service.RagScheduledService;
import com.helmsail.lightborrow.rag.strategy.retrieval.CitationGenerator;
import com.helmsail.lightborrow.rag.strategy.retrieval.HybridSearchService;
import com.helmsail.lightborrow.rag.strategy.retrieval.QueryRewriteService;
import com.helmsail.lightborrow.rag.strategy.chunking.ChunkingStrategy;
import com.helmsail.lightborrow.rag.strategy.chunking.FixedSizeChunker;
import com.helmsail.lightborrow.aiinfra.vector.VectorStore;
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
    @ConditionalOnBean(ChatModel.class)
    public RagGenerationService ragGenerationService(ChatModel chatModel) {
        return new RagGenerationService(chatModel);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({EmbeddingModel.class, VectorDocumentMapper.class, ChatModel.class})
    public RagOnlinePipeline ragOnlinePipeline(QueryRewriteService queryRewriteService,
                                               HybridSearchService hybridSearchService,
                                               RagGenerationService generationService,
                                               CitationGenerator citationGenerator,
                                               RagProperties ragProperties) {
        return new RagOnlinePipeline(queryRewriteService, hybridSearchService,
                generationService, citationGenerator, ragProperties.getTopK());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ChatModel.class)
    public QueryRewriteService queryRewriteService(ChatModel chatModel) {
        return new QueryRewriteService(chatModel);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({VectorDocumentMapper.class, EmbeddingModel.class})
    public HybridSearchService hybridSearchService(VectorDocumentMapper vectorDocumentMapper,
                                                     EmbeddingModel embeddingModel) {
        return new HybridSearchService(vectorDocumentMapper, embeddingModel);
    }

    @Bean
    @ConditionalOnMissingBean
    public CitationGenerator citationGenerator() {
        return new CitationGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RagOfflinePipeline.class)
    public RagScheduledService ragScheduledService(RagOfflinePipeline ragOfflinePipeline) {
        return new RagScheduledService(ragOfflinePipeline);
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
