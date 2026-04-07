package com.varol.journalmatch.controller;

import com.varol.journalmatch.dto.JournalSuggestRequest;
import com.varol.journalmatch.dto.TfidfJournalResponse;
import com.varol.journalmatch.service.TfidfPythonService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tfidf")
public class TfidfController {

    private final TfidfPythonService tfidfPythonService;

    public TfidfController(TfidfPythonService tfidfPythonService) {
        this.tfidfPythonService = tfidfPythonService;
    }

    @PostMapping("/suggest")
    public List<TfidfJournalResponse> suggest(@RequestBody JournalSuggestRequest request) {
        return tfidfPythonService.getTfidfRecommendations(
                request.getTitle(),
                request.getAbstractText()
        );
    }
}