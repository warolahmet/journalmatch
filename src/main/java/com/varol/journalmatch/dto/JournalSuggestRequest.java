package com.varol.journalmatch.dto;

public class JournalSuggestRequest {

    private String title;
    private String abstractText;
    private Integer limit;

    public Integer getLimit() { return limit;}

    public void setLimit(Integer limit) { this.limit = limit; }

    public String getTitle() { return title; }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }
}