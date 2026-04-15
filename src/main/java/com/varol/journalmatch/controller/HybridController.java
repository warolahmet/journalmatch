package com.varol.journalmatch.controller;

import com.varol.journalmatch.dto.HybridJournalResponse;
import com.varol.journalmatch.dto.JournalSuggestRequest;
import com.varol.journalmatch.service.HybridSuggestionService;
import com.varol.journalmatch.service.QueryEmbeddingPythonService;
import com.varol.journalmatch.service.SimpleEmbeddingSearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hybrid")
public class HybridController {

    private final HybridSuggestionService hybridSuggestionService;
    private final SimpleEmbeddingSearchService simpleEmbeddingSearchService;
    private final QueryEmbeddingPythonService queryEmbeddingPythonService;


    public HybridController(HybridSuggestionService hybridSuggestionService, SimpleEmbeddingSearchService simpleEmbeddingSearchService, QueryEmbeddingPythonService queryEmbeddingPythonService) {
        this.hybridSuggestionService = hybridSuggestionService;
        this.simpleEmbeddingSearchService = simpleEmbeddingSearchService;
        this.queryEmbeddingPythonService = queryEmbeddingPythonService;
    }

    @PostMapping("/suggest")
    public List<HybridJournalResponse> suggest(@RequestBody JournalSuggestRequest request) {
        System.out.println("REQUEST TITLE = " + request.getTitle());
        System.out.println("REQUEST ABSTRACT = " + request.getAbstractText());
        System.out.println("REQUEST LIMIT = " + request.getLimit());

        return hybridSuggestionService.suggest(
                request.getTitle(),
                request.getAbstractText(),
                request.getLimit()
        );
    }

    @PostMapping("/embedding-test")
    public List<String> test(@RequestBody double[] embedding) {
    return simpleEmbeddingSearchService.search(embedding)
            .stream()
            .map(result -> result.getName() + " | " + result.getEmbeddingScore())
            .toList();
    }

    @PostMapping("/query-embedding-test")
    public List<String> queryEmbeddingTest(@RequestBody JournalSuggestRequest request) {
        String text = (request.getTitle() == null ? "" : request.getTitle()) + " " +
                    (request.getAbstractText() == null ? "" : request.getAbstractText());

        double[] queryEmbedding = queryEmbeddingPythonService.getQueryEmbedding(text);

        return simpleEmbeddingSearchService.search(queryEmbedding)
                .stream()
                .map(result -> result.getName() + " | " + result.getEmbeddingScore())
                .toList();
    }

}