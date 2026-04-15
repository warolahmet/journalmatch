package com.varol.journalmatch.service;

import com.varol.journalmatch.dto.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HybridSuggestionService {

    private final JournalSuggestionService journalSuggestionService;
    private final QueryEmbeddingPythonService queryEmbeddingPythonService;
    private final SimpleEmbeddingSearchService simpleEmbeddingSearchService;
    private final TfidfPythonService tfidfPythonService;


    public HybridSuggestionService(
            JournalSuggestionService journalSuggestionService, QueryEmbeddingPythonService queryEmbeddingPythonService, SimpleEmbeddingSearchService simpleEmbeddingSearchService,
            TfidfPythonService tfidfPythonService
    ) {
        this.journalSuggestionService = journalSuggestionService;
        this.queryEmbeddingPythonService = queryEmbeddingPythonService;
        this.simpleEmbeddingSearchService = simpleEmbeddingSearchService;
        this.tfidfPythonService = tfidfPythonService;
    }

    public List<HybridJournalResponse> suggest(String title, String abstractText, Integer limit) {
        List<JournalResponse> ruleResults =
                journalSuggestionService.suggestJournals(title, abstractText);

        List<TfidfJournalResponse> tfidfResults = List.of();
        boolean tfidfAvailable = false;
        System.out.println("NEW EMBEDDING PIPELINE ACTIVE");


  //      List<EmbeddingJournalResponse> embeddingResults = List.of();
  //              embeddingPythonService.getEmbeddingRecommendations(title, abstractText);

        String queryText = (title == null ? "" : title) + " " +
                   (abstractText == null ? "" : abstractText);

        double[] queryEmbedding = queryEmbeddingPythonService.getQueryEmbedding(queryText);

        List<EmbeddingSimilarityResult> embeddingResults =
                simpleEmbeddingSearchService.search(queryEmbedding);

        System.out.println("Embedding results size = " + embeddingResults.size());
        System.out.println("TF-IDF available: " + tfidfAvailable);

        Set<String> queryDomainTokens = extractDomainTokens(title + " " + abstractText);

        Map<String, JournalResponse> ruleMap = new HashMap<>();
        for (JournalResponse result : ruleResults) {
            ruleMap.put(normalizeKey(result.getName()), result);
        }

        Map<String, TfidfJournalResponse> tfidfMap = new HashMap<>();
        for (TfidfJournalResponse result : tfidfResults) {
            tfidfMap.put(normalizeKey(result.getName()), result);
        }

        Map<String, EmbeddingSimilarityResult> embeddingMap = new HashMap<>();
        for (EmbeddingSimilarityResult result : embeddingResults) {
            embeddingMap.put(normalizeKey(result.getName()), result);
        }

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(ruleMap.keySet());
        allKeys.addAll(tfidfMap.keySet());
        allKeys.addAll(embeddingMap.keySet());

        Map<String, HybridJournalResponse> dedupedResults = new HashMap<>();

        for (String key : allKeys) {
            JournalResponse rule = ruleMap.get(key);
            TfidfJournalResponse tfidf = tfidfMap.get(key);

            if (tfidf == null) {
                for (TfidfJournalResponse candidate : tfidfResults) {
                    String candidateName = normalizeKey(candidate.getName());

                    if (candidateName.contains(key) || key.contains(candidateName)) {
                        tfidf = candidate;
                        break;
                    }
                }
            }

            EmbeddingSimilarityResult embedding = embeddingMap.get(key);

            if (embedding == null) {
                for (EmbeddingSimilarityResult candidate : embeddingResults) {
                    String candidateName = normalizeKey(candidate.getName());

                    if (candidateName.contains(key) || key.contains(candidateName)) {
                        embedding = candidate;
                        break;
                    }
                }
            }

            if (rule == null && tfidf == null && embedding == null) {
                continue;
            }

            String name = rule != null
                    ? rule.getName()
                    : (tfidf != null ? tfidf.getName() : embedding.getName());

            String publisher = rule != null
                    ? rule.getPublisher()
                    : (tfidf != null ? tfidf.getPublisher() : embedding.getPublisher());

            String subjectArea = rule != null
                    ? rule.getSubjectArea()
                    : (tfidf != null ? tfidf.getSubjectArea() : embedding.getSubjectArea());

            Boolean openAccess = rule != null
                    ? rule.getOpenAccess()
                    : (tfidf != null ? tfidf.getOpenAccess() : embedding.getOpenAccess());

            Integer apcUsd = rule != null
                    ? rule.getApcUsd()
                    : (tfidf != null ? tfidf.getApcUsd() : embedding.getApcUsd());

            int ruleScore = rule != null ? rule.getScore() : 0;
            ruleScore = Math.min(ruleScore, 10);
            double tfidfScore = tfidf != null ? tfidf.getTfidfScore() : 0.0;
            double embeddingScore = embedding != null ? embedding.getEmbeddingScore() : 0.0;
            String matchedKeywords = rule != null ? rule.getMatchedKeywords() : "";
            double hybridScore;

            if (embeddingScore > 0 || tfidfScore > 0) {
                hybridScore = (ruleScore * 0.4) + (tfidfScore * 20.0) + (embeddingScore * 40.0);
            } else if (tfidfAvailable) {
                hybridScore = (ruleScore * 0.5) + (tfidfScore * 30.0);
            } else {
                hybridScore = ruleScore;
            }

            String journalDomainText =
                    (subjectArea == null ? "" : subjectArea) + " " +
                            (matchedKeywords == null ? "" : matchedKeywords);

            Set<String> journalDomainTokens = extractDomainTokens(journalDomainText);
            int domainOverlap = calculateDomainOverlap(queryDomainTokens, journalDomainTokens);

            if (domainOverlap >= 2) {
                hybridScore += 2;
            } else if (domainOverlap == 1) {
                hybridScore += 1;
            } else {
                hybridScore -= 2;
            }

            int strongMatchCount = 0;

            for (String token : queryDomainTokens) {
                if (token.length() >= 7 && journalDomainTokens.contains(token)) {
                    strongMatchCount++;
                }
            }

            if (strongMatchCount >= 1) {
                hybridScore += 2;
            }

            HybridJournalResponse current = new HybridJournalResponse(
                    name,
                    publisher,
                    subjectArea,
                    openAccess,
                    apcUsd,
                    ruleScore,
                    tfidfScore,
                    embeddingScore,
                    hybridScore,
                    matchedKeywords
            );

            String normalizedName = normalizeKey(name);
            HybridJournalResponse existing = dedupedResults.get(normalizedName);

            if (existing == null || current.getHybridScore() > existing.getHybridScore()) {
                dedupedResults.put(normalizedName, current);
            }
        }

        List<HybridJournalResponse> combinedResults = new ArrayList<>(dedupedResults.values());

        combinedResults.sort(Comparator.comparingDouble(HybridJournalResponse::getHybridScore).reversed());

        int finalLimit = (limit != null && limit > 0) ? limit : 5;
        System.out.println("allKeys size = " + allKeys.size());
        System.out.println("dedupedResults size = " + dedupedResults.size());
        System.out.println("finalLimit = " + finalLimit);
        System.out.println("combinedResults size = " + combinedResults.size());

        return combinedResults.stream().limit(finalLimit).toList();

    }

    private Set<String> extractDomainTokens(String text) {
        Set<String> tokens = new HashSet<>();

        String normalized = normalizeKey(text);
        if (normalized.isBlank()) {
            return tokens;
        }

        String[] parts = normalized.split(" ");
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }

            if (part.length() < 5) {
                continue;
            }

            if (isGenericToken(part)) {
                continue;
            }

            tokens.add(part);
        }

        return tokens;
    }

    private boolean isGenericToken(String token) {
        return Set.of(
                "machine",
                "learning",
                "artificial",
                "intelligence",
                "computer",
                "science",
                "medical",
                "methods",
                "programs",
                "advanced",
                "current",
                "journal",
                "analysis",
                "studies",
                "modeling",
                "techniques",
                "applications",
                "network",
                "networks"
        ).contains(token);
    }

    private int calculateDomainOverlap(Set<String> queryTokens, Set<String> journalTokens) {
        int overlap = 0;

        for (String token : queryTokens) {
            if (journalTokens.contains(token)) {
                overlap++;
            }
        }

        return overlap;
    }

    private String normalizeKey(String text) {
        if (text == null) {
            return "";
        }

        return text.toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}