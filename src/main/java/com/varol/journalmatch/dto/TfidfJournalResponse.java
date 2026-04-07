package com.varol.journalmatch.dto;

public class TfidfJournalResponse {

    private String name;
    private String publisher;
    private String subjectArea;
    private Boolean openAccess;
    private Integer apcUsd;
    private Double tfidfScore;

    public TfidfJournalResponse() {
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

    public Double getTfidfScore() {
        return tfidfScore;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setSubjectArea(String subjectArea) {
        this.subjectArea = subjectArea;
    }

    public void setOpenAccess(Boolean openAccess) {
        this.openAccess = openAccess;
    }

    public void setApcUsd(Integer apcUsd) {
        this.apcUsd = apcUsd;
    }

    public void setTfidfScore(Double tfidfScore) {
        this.tfidfScore = tfidfScore;
    }
}