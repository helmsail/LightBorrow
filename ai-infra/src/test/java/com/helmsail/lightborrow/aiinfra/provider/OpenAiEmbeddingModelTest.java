package com.helmsail.lightborrow.aiinfra.provider;

import com.helmsail.lightborrow.aiinfra.config.AiProperties;
import com.helmsail.lightborrow.aiinfra.model.EmbeddingResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiEmbeddingModelTest {

    @Test
    void shouldCreateInstance() {
        var properties = new AiProperties.EmbeddingProperties();
        properties.setBaseUrl("https://api.test.com");
        properties.setApiKey("test-key");

        var model = new OpenAiEmbeddingModel(RestClient.create(), properties);

        assertThat(model).isNotNull();
    }

    @Test
    void embeddingResponseShouldReturnEmbeddingFromFirstData() {
        var data = new EmbeddingResponse.EmbeddingData(0, new float[]{0.1f, 0.2f, 0.3f});
        var response = new EmbeddingResponse(List.of(data), null);

        float[] embedding = response.embedding();
        assertThat(embedding).containsExactly(0.1f, 0.2f, 0.3f);
    }

    @Test
    void embeddingResponseShouldReturnEmptyForNullData() {
        var response = new EmbeddingResponse(null, null);
        assertThat(response.embedding()).isEmpty();
    }

    @Test
    void embeddingResponseShouldReturnEmptyForEmptyData() {
        var response = new EmbeddingResponse(List.of(), null);
        assertThat(response.embedding()).isEmpty();
    }

    @Test
    void embeddingResponseShouldReturnEmbeddingsList() {
        var data1 = new EmbeddingResponse.EmbeddingData(0, new float[]{0.1f});
        var data2 = new EmbeddingResponse.EmbeddingData(1, new float[]{0.2f});
        var response = new EmbeddingResponse(List.of(data1, data2), null);

        List<float[]> embeddings = response.embeddings();
        assertThat(embeddings).hasSize(2);
        assertThat(embeddings.get(0)).containsExactly(0.1f);
        assertThat(embeddings.get(1)).containsExactly(0.2f);
    }

    @Test
    void embeddingResponseShouldReturnEmptyEmbeddingsForNullData() {
        var response = new EmbeddingResponse(null, null);
        assertThat(response.embeddings()).isEmpty();
    }
}
