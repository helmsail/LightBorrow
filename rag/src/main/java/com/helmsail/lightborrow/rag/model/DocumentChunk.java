package com.helmsail.lightborrow.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class DocumentChunk {

    private String id;
    private String documentId;
    private String content;
    private Map<String, Object> metadata;
    private float[] embedding;
    private int chunkIndex;
}
