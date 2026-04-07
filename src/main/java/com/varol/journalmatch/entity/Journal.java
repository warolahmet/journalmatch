package com.varol.journalmatch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "journals")
public class Journal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String publisher;

    private String issn;

    private Integer hIndex;

    @Column(columnDefinition = "TEXT")
    private String subjectArea;
    private Boolean openAccess;

    @Column(columnDefinition = "TEXT")
    private String openalexId;

    public String getOpenalexId() {
        return openalexId;
    }

    public void setOpenalexId(String openalexId) {
        this.openalexId = openalexId;
    }

    public Integer getWorksCount() {
        return worksCount;
    }

    public void setWorksCount(Integer worksCount) {
        this.worksCount = worksCount;
    }

    public Boolean getOa() {
        return isOa;
    }

    public void setOa(Boolean oa) {
        isOa = oa;
    }

    public Boolean getInDoaj() {
        return isInDoaj;
    }

    public void setInDoaj(Boolean inDoaj) {
        isInDoaj = inDoaj;
    }

    private Integer worksCount;
    private Boolean isOa;
    private Boolean isInDoaj;

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

    private Integer apcUsd;

    @Column(columnDefinition = "TEXT")
    private String keywords;

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Long getId() {
        return id;
    }

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

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public Integer getHIndex() {
        return hIndex;
    }

    public void setHIndex(Integer hIndex) {
        this.hIndex = hIndex;
    }
}