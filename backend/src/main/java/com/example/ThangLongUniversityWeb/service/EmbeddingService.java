package com.example.ThangLongUniversityWeb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;

/**
 * Calls HuggingFace Inference API to produce dense embeddings.
 *
 * Model: sentence-transformers/paraphrase-multilingual-mpnet-base-v2
 * Output dimension: 768
 * Supports Vietnamese and 50+ languages.
 *
 * Free tier: no API key required for public models (rate-limited).
 * To increase limits set embedding.api.key in application.properties.
 */
@Service
@Slf4j
public class EmbeddingService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${embedding.api.key:}")
    private String apiKey;

    @Value("${embedding.dimension:768}")
    private int dimension;

    public EmbeddingService(@Qualifier("embeddingRestClient") RestClient restClient,
                            ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Embed a single text. Returns float[dimension] or null on error.
     */
    public float[] embed(String text) {
        List<float[]> results = embedBatch(List.of(text));
        return (results != null && !results.isEmpty()) ? results.get(0) : null;
    }

    /**
     * Embed multiple texts in one API call.
     * Returns a list of float[dimension], one per input text.
     */
    public List<float[]> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) return List.of();
        try {
            // HuggingFace feature-extraction endpoint
            // Request body: {"inputs": ["text1", "text2"]}
            // Response: [[float, ...], [float, ...]]
            String body = objectMapper.writeValueAsString(
                    objectMapper.createObjectNode().set("inputs",
                            objectMapper.valueToTree(texts))
            );

            String response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            // Parse response: [[float, ...], ...]
            float[][] parsed = objectMapper.readValue(response, float[][].class);
            return Arrays.stream(parsed).toList();

        } catch (RestClientException e) {
            log.warn("Embedding API call failed (RestClient): {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Embedding parse failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Serialize a float[] embedding to the JSON string format stored in DB: "[0.1,-0.2,...]"
     */
    public static String toJson(float[] embedding) {
        if (embedding == null) return null;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        return sb.append("]").toString();
    }

    /**
     * Deserialize DB JSON string back to float[].
     */
    public static float[] fromJson(String json) {
        if (json == null || json.isBlank()) return null;
        String inner = json.trim();
        if (inner.startsWith("[")) inner = inner.substring(1);
        if (inner.endsWith("]")) inner = inner.substring(0, inner.length() - 1);
        String[] parts = inner.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }
}
