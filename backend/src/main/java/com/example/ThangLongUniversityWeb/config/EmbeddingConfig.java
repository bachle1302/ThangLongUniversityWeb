package com.example.ThangLongUniversityWeb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class EmbeddingConfig {

    @Value("${embedding.api.url:https://api-inference.huggingface.co/pipeline/feature-extraction/sentence-transformers/paraphrase-multilingual-mpnet-base-v2}")
    private String apiUrl;

    @Value("${embedding.api.key:}")
    private String apiKey;

    @Bean("embeddingRestClient")
    public RestClient embeddingRestClient() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Content-Type", "application/json");
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + apiKey);
        }
        return builder.build();
    }
}
