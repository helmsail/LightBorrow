package com.helmsail.lightborrow.aiinfra.vector;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 向量文档。id + embedding + metadata + 可选距离。
 */
@Data
@AllArgsConstructor
public class VectorDocument {

    private String id;
    private float[] embedding;
    private String metadata;
    private Double distance;

    public VectorDocument(String id, float[] embedding, String metadata) {
        this.id = id;
        this.embedding = embedding;
        this.metadata = metadata;
    }
}
