package com.helmsail.lightborrow.rag.strategy.retriever;

import com.helmsail.lightborrow.rag.model.DocumentChunk;

import java.util.List;

public interface RetrievalStrategy {

    /**
     * 检索最相关的文档块。
     *
     * @param queryEmbedding 查询向量
     * @param candidates     候选文档块
     * @param topK           返回 topK 个结果
     * @return 排序后的文档块列表
     */
    List<DocumentChunk> retrieve(float[] queryEmbedding, List<DocumentChunk> candidates, int topK);
}
