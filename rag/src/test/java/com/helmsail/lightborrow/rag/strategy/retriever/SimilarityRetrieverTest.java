package com.helmsail.lightborrow.rag.strategy.retriever;

import com.helmsail.lightborrow.rag.config.RagProperties;
import com.helmsail.lightborrow.rag.model.DocumentChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimilarityRetrieverTest {

    private SimilarityRetriever retriever;

    @BeforeEach
    void setUp() {
        RagProperties props = new RagProperties();
        props.setSimilarityThreshold(0.5);
        retriever = new SimilarityRetriever(props);
    }

    @Test
    void shouldReturnIdentityForSameVector() {
        float[] query = {1.0f, 0.0f, 0.0f, 0.0f};
        List<DocumentChunk> candidates = List.of(chunkWithEmbedding("id1", query));
        List<DocumentChunk> result = retriever.retrieve(query, candidates, 10);
        assertThat(result).hasSize(1);
    }

    @Test
    void cosineSimilarityShouldBeApproximatelyOneForIdenticalVectors() {
        float[] vec = {1.0f, 2.0f, 3.0f, 4.0f};
        List<DocumentChunk> candidates = List.of(chunkWithEmbedding("id1", vec));
        List<DocumentChunk> result = retriever.retrieve(vec, candidates, 10);
        assertThat(result).hasSize(1);
    }

    @Test
    void cosineSimilarityShouldBeApproximatelyZeroForOrthogonalVectors() {
        float[] query = {1.0f, 0.0f, 0.0f, 0.0f};
        float[] orthogonal = {0.0f, 1.0f, 0.0f, 0.0f};
        List<DocumentChunk> candidates = List.of(chunkWithEmbedding("id1", orthogonal));
        // threshold=0.5, orthogonal→0 < 0.5, should be filtered
        List<DocumentChunk> result = retriever.retrieve(query, candidates, 10);
        assertThat(result).isEmpty();
    }

    @Test
    void cosineSimilarityShouldBeNegativeForOppositeVectors() {
        float[] query = {1.0f, 0.0f};
        float[] opposite = {-1.0f, 0.0f};
        List<DocumentChunk> candidates = List.of(chunkWithEmbedding("id1", opposite));
        List<DocumentChunk> result = retriever.retrieve(query, candidates, 10);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyForMismatchedDimensions() {
        float[] query = {1.0f, 0.0f, 0.0f};
        float[] candidate = {1.0f, 0.0f};
        List<DocumentChunk> candidates = List.of(chunkWithEmbedding("id1", candidate));
        List<DocumentChunk> result = retriever.retrieve(query, candidates, 10);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNullQuery() {
        List<DocumentChunk> result = retriever.retrieve(null, List.of(), 10);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNullCandidates() {
        float[] query = {1.0f, 0.0f};
        List<DocumentChunk> result = retriever.retrieve(query, null, 10);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyForEmptyCandidates() {
        float[] query = {1.0f, 0.0f};
        List<DocumentChunk> result = retriever.retrieve(query, new ArrayList<>(), 10);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldRespectThresholdFilter() {
        RagProperties strictProps = new RagProperties();
        strictProps.setSimilarityThreshold(0.99);
        SimilarityRetriever strictRetriever = new SimilarityRetriever(strictProps);

        float[] query = {1.0f, 0.0f, 0.0f};
        float[] close = {1.0f, 1.0f, 0.0f};  // cos = 1/sqrt(2) = 0.707 < 0.99
        float[] exact = {3.0f, 0.0f, 0.0f};  // cos = 1.0 >= 0.99

        List<DocumentChunk> candidates = List.of(
                chunkWithEmbedding("close", close),
                chunkWithEmbedding("exact", exact)
        );
        List<DocumentChunk> result = strictRetriever.retrieve(query, candidates, 10);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("exact");
    }

    @Test
    void shouldLimitResultsToTopK() {
        float[] query = {1.0f, 0.0f};
        List<DocumentChunk> candidates = List.of(
                chunkWithEmbedding("id1", new float[]{1.0f, 0.0f}),
                chunkWithEmbedding("id2", new float[]{0.99f, 0.01f}),
                chunkWithEmbedding("id3", new float[]{0.98f, 0.02f})
        );
        List<DocumentChunk> result = retriever.retrieve(query, candidates, 2);
        assertThat(result).hasSize(2);
    }

    @Test
    void shouldRankBySimilarityDescending() {
        float[] query = {1.0f, 0.0f};
        List<DocumentChunk> candidates = List.of(
                chunkWithEmbedding("low", new float[]{0.6f, 0.4f}),
                chunkWithEmbedding("high", new float[]{0.99f, 0.01f}),
                chunkWithEmbedding("medium", new float[]{0.8f, 0.2f})
        );
        List<DocumentChunk> result = retriever.retrieve(query, candidates, 10);
        // Should be sorted by similarity descending: high > medium > low
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo("high");
        assertThat(result.get(1).getId()).isEqualTo("medium");
        assertThat(result.get(2).getId()).isEqualTo("low");
    }

    @Test
    void shouldHandleZeroVectors() {
        float[] query = {0.0f, 0.0f};
        float[] candidate = {1.0f, 1.0f};
        List<DocumentChunk> candidates = List.of(chunkWithEmbedding("id1", candidate));
        List<DocumentChunk> result = retriever.retrieve(query, candidates, 10);
        assertThat(result).isEmpty();
    }

    private static DocumentChunk chunkWithEmbedding(String id, float[] embedding) {
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(id);
        chunk.setEmbedding(embedding);
        chunk.setContent("content_" + id);
        return chunk;
    }
}
