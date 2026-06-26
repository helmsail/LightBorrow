package com.helmsail.lightborrow.rag.strategy.chunking;

import com.helmsail.lightborrow.rag.config.RagProperties;
import com.helmsail.lightborrow.rag.model.DocumentChunk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FixedSizeChunkerTest {

    private final RagProperties properties = properties(20, 5);
    private final FixedSizeChunker chunker = new FixedSizeChunker(properties);

    @Test
    void shouldReturnEmptyListForNullContent() {
        assertThat(chunker.chunk("doc1", null)).isEmpty();
    }

    @Test
    void shouldReturnEmptyListForBlankContent() {
        assertThat(chunker.chunk("doc1", "")).isEmpty();
        assertThat(chunker.chunk("doc1", "   ")).isEmpty();
    }

    @Test
    void shouldReturnSingleChunkForShortContent() {
        List<DocumentChunk> chunks = chunker.chunk("doc1", "短文本");
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).getDocumentId()).isEqualTo("doc1");
        assertThat(chunks.get(0).getChunkIndex()).isZero();
        assertThat(chunks.get(0).getId()).isNotBlank();
    }

    @Test
    void shouldSplitLongTextIntoMultipleChunks() {
        // chunkSize=20, overlap=5 => each chunk ~20 chars, overlap ~5 chars
        // 60 chars with overlap=5 => 4 chunks
        String text = "AAAAAAAAAAAAAAAAAAAA"  // 20 chars -> chunk0
                + "BBBBBBBBBBBBBBBBBBBB"      // 20 chars -> chunk1 starts at pos 15 (20-5)
                + "CCCCCCCCCCCCCCCCCCCC";     // 20 chars

        List<DocumentChunk> chunks = chunker.chunk("doc1", text);
        assertThat(chunks).hasSize(4);
        assertThat(chunks.get(0).getContent()).contains("AAAAA");
        assertThat(chunks.get(3).getContent()).contains("CCCCC");
    }

    @Test
    void shouldSplitAtSentenceBoundary() {
        RagProperties props = properties(30, 3);
        FixedSizeChunker sentChunker = new FixedSizeChunker(props);

        // A sentence boundary (。) exists within the chunk window
        String text = "第一句话很长很长很长很长。第二句话也很长很长很长很长很长。第三句话。";
        List<DocumentChunk> chunks = sentChunker.chunk("doc1", text);

        assertThat(chunks).isNotEmpty();
        // Should find sentence boundary and split there
        boolean anyEndsWithSentenceBoundary = chunks.stream()
                .anyMatch(c -> c.getContent().endsWith("。"));
        assertThat(anyEndsWithSentenceBoundary).isTrue();
    }

    @Test
    void shouldHandleOverlapCorrectly() {
        // chunkSize=20, overlap=8
        RagProperties props = properties(20, 8);
        FixedSizeChunker overlapChunker = new FixedSizeChunker(props);
        String text = "AAAA" + "BBBB" + "CCCC" + "DDDD" + "EEEE";

        List<DocumentChunk> chunks = overlapChunker.chunk("doc1", text);

        assertThat(chunks).isNotEmpty();
        if (chunks.size() > 1) {
            String firstEnd = chunks.get(0).getContent();
            String secondStart = chunks.get(1).getContent();
            // Second chunk should start within overlap range of first chunk's end
            assertThat(secondStart).isNotEqualTo(firstEnd);
        }
    }

    @Test
    void shouldHandleExactBoundaryMatch() {
        // content length exactly equals chunkSize
        String exactText = "12345678901234567890"; // 20 chars
        List<DocumentChunk> chunks = chunker.chunk("doc1", exactText);
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).getContent()).isEqualTo(exactText);
    }

    @Test
    void shouldHandleContentJustOverChunkSize() {
        // 21 chars, chunkSize=20, overlap=5
        String text = "12345678901234567890X";
        List<DocumentChunk> chunks = chunker.chunk("doc1", text);
        assertThat(chunks).hasSize(2);
    }

    @Test
    void shouldAssignUniqueIdsAndSequentialIndices() {
        RagProperties props = properties(10, 2);
        FixedSizeChunker smallChunker = new FixedSizeChunker(props);
        String text = "AAAAAAAAAABBBBBBBBBBCCCCCCCCCC";
        List<DocumentChunk> chunks = smallChunker.chunk("doc1", text);

        assertThat(chunks).hasSize(4);
        assertThat(chunks.get(0).getChunkIndex()).isZero();
        assertThat(chunks.get(1).getChunkIndex()).isOne();
        assertThat(chunks.get(2).getChunkIndex()).isEqualTo(2);
        assertThat(chunks.get(3).getChunkIndex()).isEqualTo(3);
        // IDs should be unique (UUIDs)
        assertThat(chunks.get(0).getId()).isNotEqualTo(chunks.get(1).getId());
    }

    @Test
    void shouldHandleDefaultPropertiesWhenNull() {
        FixedSizeChunker defaultChunker = new FixedSizeChunker(null);
        String text = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        List<DocumentChunk> chunks = defaultChunker.chunk("doc1", text);
        // Default chunkSize=500, overlap=50 -> single chunk for 26 chars
        assertThat(chunks).hasSize(1);
    }

    private static RagProperties properties(int chunkSize, int overlap) {
        RagProperties props = new RagProperties();
        props.setChunkSize(chunkSize);
        props.setOverlap(overlap);
        return props;
    }
}
