package com.varol.journalmatch.service;

import com.varol.journalmatch.entity.Journal;
import com.varol.journalmatch.repository.JournalRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class JournalCsvImporter {

    private final JournalRepository journalRepository;

    public JournalCsvImporter(JournalRepository journalRepository) {
        this.journalRepository = journalRepository;
    }

    public void importCsv(String filePath) {
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                if (parts.length < 12) {
                    continue;
                }

                Journal journal = new Journal();
                journal.setName(clean(parts[0]));
                journal.setPublisher(clean(parts[1]));
                journal.setIssn(clean(parts[2]));
                journal.setHIndex(parseInteger(parts[3]));
                journal.setKeywords(clean(parts[4]));
                journal.setSubjectArea(clean(parts[5]));
                journal.setOpenAccess(parseBoolean(parts[6]));
                journal.setApcUsd(parseInteger(parts[7]));
                journal.setOpenalexId(clean(parts[8]));
                journal.setWorksCount(parseInteger(parts[9]));
                journal.setOa(parseBoolean(parts[10]));
                journal.setInDoaj(parseBoolean(parts[11]));

                journalRepository.save(journal);
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\"", "").trim();
    }

    private Integer parseInteger(String value) {
        try {
            String cleaned = clean(value);
            if (cleaned == null || cleaned.isBlank()) {
                return 0;
            }
            return Integer.parseInt(cleaned);
        } catch (Exception e) {
            return 0;
        }
    }

    private Boolean parseBoolean(String value) {
        String cleaned = clean(value);
        return "true".equalsIgnoreCase(cleaned);
    }
}