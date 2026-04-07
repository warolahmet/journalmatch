package com.varol.journalmatch.controller;

import com.varol.journalmatch.entity.Journal;
import com.varol.journalmatch.repository.JournalRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JournalController {

    private final JournalRepository journalRepository;

    public JournalController(JournalRepository journalRepository) {
        this.journalRepository = journalRepository;
    }

    @GetMapping("/journals")
    public List<Journal> getAllJournals() {
        return journalRepository.findAll();
    }

    @GetMapping("/journals/count")
    public long getJournalCount() {
        return journalRepository.count();
    }
}