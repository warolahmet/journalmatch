package com.varol.journalmatch.dto;

public class EmbeddingSimilarityResult extends EmbeddingJournalResponse {

    private String name;
    private String publisher;
    private String subjectArea;
    private Boolean openAccess;
    private Integer apcUsd;
    private Double embeddingScore;

    public EmbeddingSimilarityResult(
            String name,
            String publisher,
            String subjectArea,
            Boolean openAccess,
            Integer apcUsd,
            Double embeddingScore
    ) {
        this.name = name;
        this.publisher = publisher;
        this.subjectArea = subjectArea;
        this.openAccess = openAccess;
        this.apcUsd = apcUsd;
        this.embeddingScore = embeddingScore;
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

    public Double getEmbeddingScore() {
        return embeddingScore;
    }
}