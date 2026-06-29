package com.helmsail.lightborrow.rag.pipeline.online;

import com.helmsail.lightborrow.aiinfra.embedding.EmbeddingModel;
import com.helmsail.lightborrow.rag.model.DocumentChunk;
import com.helmsail.lightborrow.rag.service.RagGenerationService;
import com.helmsail.lightborrow.rag.strategy.retrieval.CitationGenerator;
import com.helmsail.lightborrow.rag.strategy.retrieval.HybridSearchService;
import com.helmsail.lightborrow.rag.strategy.retrieval.QueryRewriteService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RagOnlinePipeline {

    private final QueryRewriteService queryRewriteService;
    private final HybridSearchService hybridSearchService;
    private final RagGenerationService generationService;
    private final CitationGenerator citationGenerator;
    private final int topK;

    public RagOnlinePipeline(QueryRewriteService queryRewriteService,
                             HybridSearchService hybridSearchService,
                             RagGenerationService generationService,
                             CitationGenerator citationGenerator,
                             int topK) {
        this.queryRewriteService = queryRewriteService;
        this.hybridSearchService = hybridSearchService;
        this.generationService = generationService;
        this.citationGenerator = citationGenerator;
        this.topK = topK;
    }

    /**
     * 执行在线检索与生成。
     *
     * @param query 用户查询
     * @return 生成的回答
     */
    public String execute(String query) {
        log.info("[RAG] 在线 Pipeline: query={}", query);

        // Step 0: Query Rewrite
        String rewrittenQuery = queryRewriteService.rewrite(query);
        log.debug("[RAG] 查询重写: '{}' -> '{}'", query, rewrittenQuery);

        // Step 1: Hybrid Search (vector + keyword)
        int vectorTopK = topK * 2;
        int keywordTopK = topK;
        List<DocumentChunk> chunks = hybridSearchService.search(rewrittenQuery, vectorTopK, keywordTopK, topK);
        log.info("[RAG] 混合检索到 {} 个文档块", chunks.size());

        if (chunks.isEmpty()) {
            return "未找到相关知识。";
        }

        // Step 2: Citation Generation
        String citedContext = citationGenerator.buildCitedContext(chunks);
        String citationInstruction = citationGenerator.getCitationInstruction();

        // Step 3: Generate answer with citations
        String answer = generationService.generate(query, chunks, citedContext, citationInstruction);
        log.info("[RAG] 在线 Pipeline 完成");
        return answer;
    }
}
