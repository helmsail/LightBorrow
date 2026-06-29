package com.helmsail.lightborrow.rag.strategy.retrieval;

import com.helmsail.lightborrow.rag.model.DocumentChunk;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CitationGeneratorTest {

    private final CitationGenerator generator = new CitationGenerator();

    @Test
    void shouldBuildCitedContext() {
        List<DocumentChunk> chunks = List.of(
                DocumentChunk.builder().id("1").documentId("doc1").content("第一条内容").build(),
                DocumentChunk.builder().id("2").documentId("doc2").content("第二条内容").build()
        );

        String context = generator.buildCitedContext(chunks);
        assertThat(context).contains("[1]");
        assertThat(context).contains("第一条内容");
        assertThat(context).contains("doc1");
        assertThat(context).contains("[2]");
        assertThat(context).contains("doc2");
    }

    @Test
    void shouldReturnEmptyForNullChunks() {
        assertThat(generator.buildCitedContext(null)).isEmpty();
    }

    @Test
    void shouldHandleChunksWithoutDocumentId() {
        List<DocumentChunk> chunks = List.of(
                DocumentChunk.builder().id("1").documentId("").content("内容").build()
        );

        String context = generator.buildCitedContext(chunks);
        assertThat(context).contains("来源1");
    }

    @Test
    void citationInstructionShouldContainKeyPhrases() {
        String instruction = generator.getCitationInstruction();
        assertThat(instruction).contains("[1]");
        assertThat(instruction).contains("来源");
    }
}
