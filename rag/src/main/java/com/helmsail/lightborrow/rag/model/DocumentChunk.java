package com.helmsail.lightborrow.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk {

    /** 文档块唯一 ID */
    private String id;

    /** 原始文档 ID */
    private String documentId;

    /** 文本内容 */
    private String content;

    /** 元数据 */
    private Map<String, Object> metadata;

    /** 向量嵌入（float 数组） */
    private float[] embedding;

    /** 块序号 */
    private int chunkIndex;
}
