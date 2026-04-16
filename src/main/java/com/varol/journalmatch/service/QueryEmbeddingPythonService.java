package com.varol.journalmatch.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class QueryEmbeddingPythonService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL = "http://127.0.0.1:8001/embed";

    public double[] getQueryEmbedding(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of("text", text);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<EmbeddingResponse> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<EmbeddingResponse>() {}
            );

            if (response.getBody() == null || response.getBody().getEmbedding() == null) {
                throw new RuntimeException("Embedding API returned empty response");
            }

            List<Double> embeddingList = response.getBody().getEmbedding();
            double[] result = new double[embeddingList.size()];

            for (int i = 0; i < embeddingList.size(); i++) {
                result[i] = embeddingList.get(i);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate query embedding via local API", e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmbeddingResponse {
        private List<Double> embedding;

        public List<Double> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(List<Double> embedding) {
            this.embedding = embedding;
        }
    }
}