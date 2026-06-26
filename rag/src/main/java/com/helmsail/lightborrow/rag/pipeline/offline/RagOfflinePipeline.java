package com.helmsail.lightborrow.rag.pipeline.offline;

import com.helmsail.lightborrow.aiinfra.embedding.EmbeddingModel;
import com.helmsail.lightborrow.aiinfra.vector.VectorDocument;
import com.helmsail.lightborrow.aiinfra.vector.VectorStore;
import com.helmsail.lightborrow.framework.util.JsonUtil;
import com.helmsail.lightborrow.rag.model.DocumentChunk;
import com.helmsail.lightborrow.rag.strategy.chunking.ChunkingStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class RagOfflinePipeline {

    private final ChunkingStrategy chunkingStrategy;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    public RagOfflinePipeline(ChunkingStrategy chunkingStrategy,
                              EmbeddingModel embeddingModel,
                              VectorStore vectorStore) {
        this.chunkingStrategy = chunkingStrategy;
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    /**
     * 处理单个文档：分块 → 向量化 → 存储。
     *
     * @param documentId 文档 ID
     * @param content    文档内容
     * @return 文档块列表
     */
    public List<DocumentChunk> processDocument(String documentId, String content) {
        // Step 1: Chunk
        List<DocumentChunk> chunks = chunkingStrategy.chunk(documentId, content);
        log.info("[RAG] 离线 Pipeline: 文档 {} 分块 {} 个", documentId, chunks.size());

        if (chunks.isEmpty()) {
            return chunks;
        }

        // Step 2: Embed each chunk
        List<String> texts = chunks.stream()
                .map(DocumentChunk::getContent)
                .toList();
        List<float[]> embeddings = embeddingModel.embedBatch(texts);

        // Step 3: Build VectorDocuments
        List<VectorDocument> documents = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            float[] embedding = i < embeddings.size() ? embeddings.get(i) : null;
            if (embedding == null || embedding.length == 0) {
                log.warn("[RAG] 分块 {} 向量化为空，跳过", chunk.getId());
                continue;
            }
            chunk.setEmbedding(embedding);

            // Store content + metadata as JSON in VectorDocument.metadata
            String metadata = JsonUtil.toJson(Map.of(
                    "content", chunk.getContent(),
                    "documentId", chunk.getDocumentId(),
                    "chunkIndex", chunk.getChunkIndex()
            ));
            documents.add(new VectorDocument(chunk.getId(), embedding, metadata));
        }

        // Step 4: Store to VectorStore
        if (!documents.isEmpty()) {
            vectorStore.upsertAll(documents);
            log.info("[RAG] 离线 Pipeline: 存储 {} 个向量", documents.size());
        }

        return chunks;
    }
}
