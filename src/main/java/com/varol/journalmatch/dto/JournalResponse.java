package com.varol.journalmatch.dto;

public class JournalResponse {

    private String name;
    private String publisher;
    private String subjectArea;
    private Boolean openAccess;
    private Integer apcUsd;
    private int score;
    private String matchedKeywords;

    public JournalResponse(String name, String publisher, String subjectArea, Boolean openAccess, Integer apcUsd, int score, String matchedKeywords) {
        this.name = name;
        this.publisher = publisher;
        this.subjectArea = subjectArea;
        this.openAccess = openAccess;
        this.apcUsd = apcUsd;
        this.score = score;
        this.matchedKeywords = matchedKeywords;
    }

    public String getMatchedKeywords() {
        return matchedKeywords;
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

    public int getScore() {
        return score;
    }
}