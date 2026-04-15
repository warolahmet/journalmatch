package com.varol.journalmatch.service;

import com.varol.journalmatch.dto.EmbeddingSimilarityResult;
import com.varol.journalmatch.entity.Journal;
import com.varol.journalmatch.repository.JournalRepository;
import com.varol.journalmatch.util.EmbeddingParser;
import com.varol.journalmatch.util.SimilarityUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class SimpleEmbeddingSearchService {

    private final JournalRepository journalRepository;

    public SimpleEmbeddingSearchService(JournalRepository journalRepository) {
        this.journalRepository = journalRepository;
    }

    public List<EmbeddingSimilarityResult> search(double[] queryEmbedding) {
        List<Journal> journals = journalRepository.findAll();

        return journals.stream()
                .filter(j -> j.getEmbeddingJson() != null && !j.getEmbeddingJson().isBlank())
                .map(j -> {
                    double[] journalEmbedding = EmbeddingParser.parse(j.getEmbeddingJson());
                    double score = SimilarityUtils.cosineSimilarity(
                            toFloatArray(queryEmbedding),
                            journalEmbedding
                    );

                    return new ScoredJournal(j, score);
                })
                .sorted(Comparator.comparingDouble(ScoredJournal::score).reversed())
                .limit(20)
                .map(scored -> new EmbeddingSimilarityResult(
                        scored.journal().getName(),
                        scored.journal().getPublisher(),
                        scored.journal().getSubjectArea(),
                        scored.journal().getOpenAccess(),
                        scored.journal().getApcUsd(),
                        scored.score()
                ))
                .toList();
    }

    private float[] toFloatArray(double[] arr) {
        float[] result = new float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = (float) arr[i];
        }
        return result;
    }

    private record ScoredJournal(Journal journal, double score) {
    }
}