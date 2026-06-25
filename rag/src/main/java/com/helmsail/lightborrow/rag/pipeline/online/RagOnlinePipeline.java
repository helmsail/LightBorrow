package com.helmsail.lightborrow.rag.pipeline.online;

import com.helmsail.lightborrow.aiinfra.embedding.EmbeddingModel;
import com.helmsail.lightborrow.aiinfra.vector.VectorDocument;
import com.helmsail.lightborrow.aiinfra.vector.VectorStore;
import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.util.JsonUtil;
import com.helmsail.lightborrow.rag.exception.RagException;
import com.helmsail.lightborrow.rag.model.DocumentChunk;
import com.helmsail.lightborrow.rag.service.RagGenerationService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class RagOnlinePipeline {

    private static final int DEFAULT_TOP_K = 5;

    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    private final RagGenerationService generationService;

    public RagOnlinePipeline(EmbeddingModel embeddingModel,
                             VectorStore vectorStore,
                             RagGenerationService generationService) {
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
        this.generationService = generationService;
    }

    /**
     * 执行在线检索与生成。
     *
     * @param query 用户查询
     * @return 生成的回答
     */
    public String execute(String query) {
        log.info("[RAG] 在线 Pipeline: query={}", query);

        // Step 1: Embed query
        float[] queryEmbedding = embeddingModel.embed(query);
        if (queryEmbedding == null || queryEmbedding.length == 0) {
            throw new RagException(ErrorCode.RAG_RETRIEVAL_FAILED, "查询向量化为空");
        }

        // Step 2: Retrieve from VectorStore
        List<VectorDocument> results = vectorStore.search(queryEmbedding, DEFAULT_TOP_K);
        log.info("[RAG] 检索到 {} 个相关文档块", results.size());

        // Step 3: Convert VectorDocument -> DocumentChunk
        List<DocumentChunk> chunks = results.stream()
                .map(RagOnlinePipeline::toDocumentChunk)
                .filter(c -> c != null && c.getContent() != null)
                .toList();

        // Step 4: Generate answer
        String answer = generationService.generate(query, chunks);
        log.info("[RAG] 在线 Pipeline 完成");
        return answer;
    }

    /**
     * 将 VectorDocument（metadata 中存储了 content）转换为 DocumentChunk。
     */
    @SuppressWarnings("unchecked")
    private static DocumentChunk toDocumentChunk(VectorDocument doc) {
        if (doc.getMetadata() == null || doc.getMetadata().isBlank()) {
            return null;
        }
        try {
            Map<String, Object> meta = JsonUtil.fromJson(doc.getMetadata(), Map.class);
            DocumentChunk chunk = DocumentChunk.builder()
                    .id(doc.getId())
                    .documentId((String) meta.getOrDefault("documentId", ""))
                    .content((String) meta.get("content"))
                    .chunkIndex(meta.get("chunkIndex") instanceof Number
                            ? ((Number) meta.get("chunkIndex")).intValue() : 0)
                    .build();
            if (doc.getDistance() != null) {
                chunk.setMetadata(Map.of("distance", doc.getDistance()));
            }
            return chunk;
        } catch (Exception e) {
            log.warn("[RAG] VectorDocument metadata 解析失败 id={}", doc.getId(), e);
            return null;
        }
    }
}
