package com.varol.journalmatch.service;

import com.varol.journalmatch.dto.JournalSuggestRequest;
import com.varol.journalmatch.dto.EmbeddingJournalResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class EmbeddingPythonService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL = "http://127.0.0.1:8001/suggest";

    public List<EmbeddingJournalResponse> getEmbeddingRecommendations(String title, String abstractText) {
        try {
            JournalSuggestRequest request = new JournalSuggestRequest();
            request.setTitle(title);
            request.setAbstractText(abstractText);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<JournalSuggestRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<List<EmbeddingJournalResponse>> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<List<EmbeddingJournalResponse>>() {}
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyList();

        } catch (Exception e) {
            System.out.println("Embedding API unavailable.");
            return Collections.emptyList();
        }
    }
}