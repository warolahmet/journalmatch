package com.varol.journalmatch.service;

import com.varol.journalmatch.dto.JournalSuggestRequest;
import com.varol.journalmatch.dto.TfidfJournalResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class TfidfPythonService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PYTHON_API_URL = "http://127.0.0.1:8000/suggest";

    public List<TfidfJournalResponse> getTfidfRecommendations(String title, String abstractText) {
        try {
            JournalSuggestRequest requestBody = new JournalSuggestRequest();
            requestBody.setTitle(title);
            requestBody.setAbstractText(abstractText);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<JournalSuggestRequest> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<List<TfidfJournalResponse>> response = restTemplate.exchange(
                    PYTHON_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<List<TfidfJournalResponse>>() {}
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyList();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("TF-IDF API unavailable, falling back to rule-based results.");
            return Collections.emptyList();
        }
    }
}