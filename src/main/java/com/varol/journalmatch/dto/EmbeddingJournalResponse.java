package com.varol.journalmatch.dto;

public class EmbeddingJournalResponse {

    private String name;
    private String publisher;
    private String subjectArea;
    private Boolean openAccess;
    private Integer apcUsd;
    private Double embeddingScore;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getSubjectArea() {
        return subjectArea;
    }

    public void setSubjectArea(String subjectArea) {
        this.subjectArea = subjectArea;
    }

    public Boolean getOpenAccess() {
        return openAccess;
    }

    public void setOpenAccess(Boolean openAccess) {
        this.openAccess = openAccess;
    }

    public Integer getApcUsd() {
        return apcUsd;
    }

    public void setApcUsd(Integer apcUsd) {
        this.apcUsd = apcUsd;
    }

    public Double getEmbeddingScore() {
        return embeddingScore;
    }

    public void setEmbeddingScore(Double embeddingScore) {
        this.embeddingScore = embeddingScore;
    }
}