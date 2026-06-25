package com.helmsail.lightborrow.aiinfra.vector;

import java.util.List;

/**
 * 向量存储接口。支持向量的增删和相似度检索。
 */
public interface VectorStore {

    void upsert(VectorDocument document);

    void upsertAll(List<VectorDocument> documents);

    List<VectorDocument> search(float[] embedding, int topK);

    void delete(String id);

    void clear();
}
