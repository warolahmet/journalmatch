package com.varol.journalmatch.service;

import com.varol.journalmatch.dto.JournalResponse;
import com.varol.journalmatch.entity.Journal;
import com.varol.journalmatch.repository.JournalRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JournalSuggestionService {

    private final JournalRepository journalRepository;

    public JournalSuggestionService(JournalRepository journalRepository) {
        this.journalRepository = journalRepository;
    }

    private static final Set<String> RELEVANT_TERMS = Set.of(
            "machine learning",
            "deep learning",
            "artificial intelligence",
            "computer vision",
            "image processing",
            "neural network",
            "pattern recognition",
            "bioinformatics",
            "medical imaging",
            "data mining",
            "learning",
            "vision",
            "imaging",
            "neural",
            "medical",
            "classification",
            "recognition"
    );

    private static final Set<String> IMPORTANT_PHRASES = Set.of(
            "machine learning",
            "deep learning",
            "artificial intelligence",
            "computer vision",
            "image processing",
            "medical imaging",
            "medical image",
            "neural network",
            "pattern recognition",
            "data mining",
            "bioinformatics"
    );

    private static final Set<String> NOISY_TOKENS = Set.of(
            "analysis",
            "review",
            "study",
            "method",
            "application",
            "applications",
            "journal",
            "studies",
            "information",
            "miscellaneous",
            "model",
            "models",
            "system",
            "systems",
            "advanced",
            "diverse",
            "scientific",
            "economic"
    );

    public List<JournalResponse> suggestJournals(String title, String abstractText) {
        String safeTitle = expandText(normalize(title));
        String safeAbstractText = expandText(normalize(abstractText));

        List<Journal> journals = journalRepository.findAll();
        List<Journal> candidateJournals = journals.stream()
                .filter(this::isRelevantJournal)
                .toList();
        System.out.println("Candidate journals count: " + candidateJournals.size());

        if (candidateJournals.isEmpty()) {
            candidateJournals = journals;
        }

        List<JournalScore> scoredJournals = new ArrayList<>();

        for (Journal journal : candidateJournals) {
            MatchResult matchResult = calculateScore(
                    safeTitle,
                    safeAbstractText,
                    journal.getKeywords(),
                    journal.getSubjectArea()
            );

            scoredJournals.add(new JournalScore(
                    journal,
                    matchResult.getScore(),
                    matchResult.getMatchedKeywords()
            ));
        }

        scoredJournals.sort(
                Comparator.comparingInt(JournalScore::getScore).reversed()
                        .thenComparing((JournalScore js) -> js.getJournal().getHIndex(), Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing((JournalScore js) -> js.getJournal().getWorksCount(), Comparator.nullsLast(Comparator.reverseOrder()))
        );

        List<JournalResponse> result = new ArrayList<>();
        for (JournalScore scoredJournal : scoredJournals) {
            if (scoredJournal.getScore() <= 0) {
                continue;
            }

            Journal j = scoredJournal.getJournal();

            result.add(new JournalResponse(
                    j.getName(),
                    j.getPublisher(),
                    j.getSubjectArea(),
                    j.getOpenAccess(),
                    j.getApcUsd(),
                    scoredJournal.getScore(),
                    scoredJournal.getMatchedKeywords()
            ));

            if (result.size() == 20) {
                break;
            }
        }

        return result;
    }

    private MatchResult calculateScore(String title, String abstractText, String keywords, String subjectArea) {
        int score = 0;
        List<String> matched = new ArrayList<>();

        Set<String> titleTokens = tokenizeMeaningful(title);
        Set<String> abstractTokens = tokenizeMeaningful(abstractText);

        String combinedJournalText =
                (keywords == null ? "" : keywords) + " " +
                        (subjectArea == null ? "" : subjectArea);

        Set<String> journalTokens = tokenizeMeaningful(combinedJournalText);

        for (String token : journalTokens) {
            boolean matchedThisToken = false;

            if (titleTokens.contains(token)) {
                score += 2;
                matchedThisToken = true;
            }

            if (abstractTokens.contains(token)) {
                score += 1;
                matchedThisToken = true;
            }

            if (matchedThisToken) {
                matched.add(token);
            }
        }

        score += calculatePhraseBonus(title, abstractText, keywords, subjectArea, matched);

        return new MatchResult(score, String.join(", ", matched));
    }


    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
    private String expandText(String text) {
        String expanded = text;

        for (Map.Entry<String, List<String>> entry : SYNONYMS.entrySet()) {
            String key = entry.getKey();

            if (expanded.contains(key)) {
                for (String synonym : entry.getValue()) {
                    expanded += " " + synonym;
                }
            }
        }

        return expanded;
    }

    private static final Map<String, List<String>> SYNONYMS = Map.of(
            "deep learning", List.of("neural network", "deep neural network"),
            "machine learning", List.of("artificial intelligence", "ml"),
            "computer vision", List.of("image processing", "visual recognition"),
            "medical image", List.of("medical imaging")
    );

    private static class JournalScore {
        private final Journal journal;
        private final int score;
        private final String matchedKeywords;

        public JournalScore(Journal journal, int score, String matchedKeywords) {
            this.journal = journal;
            this.score = score;
            this.matchedKeywords = matchedKeywords;
        }

        public Journal getJournal() {
            return journal;
        }

        public int getScore() {
            return score;
        }

        public String getMatchedKeywords() {
            return matchedKeywords;
        }
    }

    private static class MatchResult {
        private final int score;
        private final String matchedKeywords;

        public MatchResult(int score, String matchedKeywords) {
            this.score = score;
            this.matchedKeywords = matchedKeywords;
        }

        public int getScore() {
            return score;
        }

        public String getMatchedKeywords() {
            return matchedKeywords;
        }
    }

    private int calculatePhraseBonus(String title, String abstractText, String keywords, String subjectArea, List<String> matched) {
        int bonus = 0;

        String combinedJournalText = normalize(
                (keywords == null ? "" : keywords) + " " +
                        (subjectArea == null ? "" : subjectArea)
        );

        for (String phrase : IMPORTANT_PHRASES) {
            String normalizedPhrase = normalize(phrase);

            if (!combinedJournalText.contains(normalizedPhrase)) {
                continue;
            }

            boolean matchedThisPhrase = false;

            if (title.contains(normalizedPhrase)) {
                bonus += 3;
                matchedThisPhrase = true;
            }

            if (abstractText.contains(normalizedPhrase)) {
                bonus += 2;
                matchedThisPhrase = true;
            }

            if (matchedThisPhrase) {
                matched.add(normalizedPhrase);
            }
        }

        return bonus;
    }

    private boolean isRelevantJournal(Journal journal) {
        String combined = normalize(
                (journal.getKeywords() == null ? "" : journal.getKeywords()) + " " +
                        (journal.getSubjectArea() == null ? "" : journal.getSubjectArea())
        );

        int hits = 0;
        for (String term : RELEVANT_TERMS) {
            if (combined.contains(term)) {
                hits++;
            }
        }

        return hits >= 1;
    }

    private Set<String> tokenizeMeaningful(String text) {
        Set<String> tokens = new HashSet<>();

        String normalized = normalize(text);
        if (normalized.isBlank()) {
            return tokens;
        }

        String[] parts = normalized.split(" ");
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }

            if (part.length() < 4) {
                continue;
            }

            if (NOISY_TOKENS.contains(part)) {
                continue;
            }

            tokens.add(part);
        }

        return tokens;
    }
}