package com.helmsail.lightborrow.rag.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "lightborrow.rag")
public class RagProperties {

    /** 分块大小（字符数） */
    private int chunkSize = 500;

    /** 重叠字符数 */
    private int overlap = 50;

    /** 检索返回 topK 条 */
    private int topK = 3;

    /** 相似度阈值 */
    private double similarityThreshold = 0.5;
}
