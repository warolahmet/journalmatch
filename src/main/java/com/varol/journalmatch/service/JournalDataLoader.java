package com.varol.journalmatch.service;

import com.varol.journalmatch.repository.JournalRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JournalDataLoader implements CommandLineRunner {

    private final JournalCsvImporter csvImporter;
    private final JournalRepository journalRepository;

    public JournalDataLoader(JournalCsvImporter csvImporter, JournalRepository journalRepository) {
        this.csvImporter = csvImporter;
        this.journalRepository = journalRepository;
    }

    @Override
    public void run(String... args) {
        if (journalRepository.count() > 0) {
            return;
        }

        csvImporter.importCsv("data/openalex_journals.csv");
    }
}