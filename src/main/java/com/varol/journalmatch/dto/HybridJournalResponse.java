package com.varol.journalmatch.dto;

public class HybridJournalResponse {

    private String name;
    private String publisher;
    private String subjectArea;
    private Boolean openAccess;
    private Integer apcUsd;
    private Integer ruleScore;
    private Double tfidfScore;
    private Double embeddingScore;
    private Double hybridScore;
    private String matchedKeywords;

    public HybridJournalResponse(
            String name,
            String publisher,
            String subjectArea,
            Boolean openAccess,
            Integer apcUsd,
            Integer ruleScore,
            Double tfidfScore,
            Double embeddingScore,
            Double hybridScore,
            String matchedKeywords
    ) {
        this.name = name;
        this.publisher = publisher;
        this.subjectArea = subjectArea;
        this.openAccess = openAccess;
        this.apcUsd = apcUsd;
        this.ruleScore = ruleScore;
        this.tfidfScore = tfidfScore;
        this.embeddingScore = embeddingScore;
        this.hybridScore = hybridScore;
        this.matchedKeywords = matchedKeywords;
    }

    public String getName() {
        return name;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getSubjectArea() {
        return subjectArea;
    }

    public Boolean getOpenAccess() {
        return openAccess;
    }

    public Integer getApcUsd() {
        return apcUsd;
    }

    public Integer getRuleScore() {
        return ruleScore;
    }

    public Double getTfidfScore() {
        return tfidfScore;
    }

    public Double getEmbeddingScore() {
        return embeddingScore;
    }

    public Double getHybridScore() {
        return hybridScore;
    }

    public String getMatchedKeywords() {
        return matchedKeywords;
    }
}