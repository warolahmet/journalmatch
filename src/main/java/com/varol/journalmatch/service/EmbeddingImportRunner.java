package com.varol.journalmatch.service;

import com.varol.journalmatch.entity.Journal;
import com.varol.journalmatch.repository.JournalRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Component
public class EmbeddingImportRunner implements CommandLineRunner {

    private final JournalRepository journalRepository;

    public EmbeddingImportRunner(JournalRepository journalRepository) {
        this.journalRepository = journalRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        boolean importEnabled = false; // iş bitince false kalacak

        if (!importEnabled) {
            return;
        }

        Path csvPath = Path.of("src/main/resources/data/openalex_journals_with_embeddings.csv");

        System.out.println("Starting embedding import from: " + csvPath);

        int updatedCount = 0;
        int skippedCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath.toFile()))) {
            String line = reader.readLine(); // header
            if (line == null) {
                System.out.println("Embedding CSV is empty.");
                return;
            }

            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                String[] parts = splitCsvLine(line);

                if (parts.length < 14) {
                    skippedCount++;
                    continue;
                }

                String name = unquote(parts[0]).trim();
                String embeddingJson = unquote(parts[13]).trim();

                if (name.isBlank() || embeddingJson.isBlank()) {
                    skippedCount++;
                    continue;
                }

                List<Journal> journals = journalRepository.findAllByName(name);

                if (!journals.isEmpty()) {
                    for (Journal journal : journals) {
                        journal.setEmbeddingJson(embeddingJson);
                        journalRepository.save(journal);
                        updatedCount++;
                    }
                } else {
                    skippedCount++;
                }
            }
        }

        System.out.println("Embedding import completed.");
        System.out.println("Updated: " + updatedCount);
        System.out.println("Skipped: " + skippedCount);
    }

    private String[] splitCsvLine(String line) {
        return line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private String unquote(String value) {
        if (value == null) {
            return "";
        }

        String trimmed = value.trim();

        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }

        return trimmed.replace("\"\"", "\"");
    }
}