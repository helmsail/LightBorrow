package com.helmsail.lightborrow.rag.strategy.chunking;

import com.helmsail.lightborrow.rag.config.RagProperties;
import com.helmsail.lightborrow.rag.model.DocumentChunk;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FixedSizeChunker implements ChunkingStrategy {

    private static final Pattern SENTENCE_BOUNDARY = Pattern.compile("[。！？.!?\\n]");
    private static final int MAX_LOOKBACK = 50;

    private final int chunkSize;
    private final int overlap;

    public FixedSizeChunker(RagProperties ragProperties) {
        // 允许 null 参数，测试中明确验证了 null 场景下的默认值行为
        this.chunkSize = ragProperties != null ? ragProperties.getChunkSize() : 500;
        this.overlap = ragProperties != null ? ragProperties.getOverlap() : 50;
    }

    @Override
    public List<DocumentChunk> chunk(String documentId, String content) {
        List<DocumentChunk> chunks = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return chunks;
        }

        int start = 0;
        int index = 0;
        while (start < content.length()) {
            int end = Math.min(start + chunkSize, content.length());

            // 如果不在文档末尾，尝试在句子边界切分
            if (end < content.length()) {
                int sentenceBoundary = findLastSentenceBoundary(content, end);
                if (sentenceBoundary > start) {
                    end = sentenceBoundary;
                }
            }

            String chunkText = content.substring(start, end);

            chunks.add(DocumentChunk.builder()
                    .id(UUID.randomUUID().toString())
                    .documentId(documentId)
                    .content(chunkText)
                    .chunkIndex(index++)
                    .build());

            if (end >= content.length()) {
                break;
            }
            start = end - overlap;
            if (start < 0) start = 0;
        }

        log.debug("[RAG] 文档分块完成 docId={}, chunks={}", documentId, chunks.size());
        return chunks;
    }

    /**
     * 从 end 位置往前查找最近的句子结束符。
     * 仅在 MAX_LOOKBACK 范围内查找，避免回退过多。
     */
    private int findLastSentenceBoundary(String content, int end) {
        int searchStart = Math.max(0, end - MAX_LOOKBACK);
        String lookback = content.substring(searchStart, end);
        Matcher matcher = SENTENCE_BOUNDARY.matcher(lookback);
        int lastBoundary = -1;
        while (matcher.find()) {
            lastBoundary = matcher.start();
        }
        if (lastBoundary >= 0) {
            return searchStart + lastBoundary + 1;
        }
        return -1;
    }
}
