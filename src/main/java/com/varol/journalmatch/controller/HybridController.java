package com.varol.journalmatch.controller;

import com.varol.journalmatch.dto.HybridJournalResponse;
import com.varol.journalmatch.dto.JournalSuggestRequest;
import com.varol.journalmatch.service.HybridSuggestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hybrid")
public class HybridController {

    private final HybridSuggestionService hybridSuggestionService;

    public HybridController(HybridSuggestionService hybridSuggestionService) {
        this.hybridSuggestionService = hybridSuggestionService;
    }

    @PostMapping("/suggest")
    public List<HybridJournalResponse> suggest(@RequestBody JournalSuggestRequest request) {
        return hybridSuggestionService.suggest(
                request.getTitle(),
                request.getAbstractText(),
                request.getLimit()
        );
    }
}