package ru.pankov.dto.statistic;

import lombok.Data;

@Data
public class SitesDetailStatistic {
    private String url;
    private String name;
    private String status;
    private String statusTime;
    private String error;
    private int pages;
    private long lemmas;

    public SitesDetailStatistic(String url, String name, String status, String statusTime, String error, int pages, long lemmas) {
        this.url = url;
        this.name = name;
        this.status = status;
        this.statusTime = statusTime;
        this.error = error;
        this.pages = pages;
        this.lemmas = lemmas;
    }
}
