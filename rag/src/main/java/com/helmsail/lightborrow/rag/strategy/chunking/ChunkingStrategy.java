package com.helmsail.lightborrow.rag.strategy.chunking;

import com.helmsail.lightborrow.rag.model.DocumentChunk;

import java.util.List;

public interface ChunkingStrategy {

    /**
     * 将文档内容分块。
     *
     * @param documentId 文档 ID
     * @param content    文档原始内容
     * @return 分块列表
     */
    List<DocumentChunk> chunk(String documentId, String content);
}
