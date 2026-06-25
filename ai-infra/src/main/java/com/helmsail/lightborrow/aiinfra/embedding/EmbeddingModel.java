package com.helmsail.lightborrow.aiinfra.embedding;

import java.util.List;

/**
 * Embedding 模型调用接口。将文本转换为向量。
 */
public interface EmbeddingModel {

    float[] embed(String text);

    List<float[]> embedBatch(List<String> texts);
}
