package com.varol.journalmatch.controller;

import com.varol.journalmatch.dto.JournalResponse;
import com.varol.journalmatch.dto.JournalSuggestRequest;
import com.varol.journalmatch.service.JournalSuggestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journals")
public class JournalSuggestionController {

    private final JournalSuggestionService journalSuggestionService;

    public JournalSuggestionController(JournalSuggestionService journalSuggestionService) {
        this.journalSuggestionService = journalSuggestionService;
    }

    @PostMapping("/suggest")
    public List<JournalResponse> suggestJournals(@RequestBody JournalSuggestRequest request) {
        return journalSuggestionService.suggestJournals(
                request.getTitle(),
                request.getAbstractText()
        );
    }
}