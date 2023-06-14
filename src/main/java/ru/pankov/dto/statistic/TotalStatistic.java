package ru.pankov.dto.statistic;

import lombok.Data;

@Data
public class TotalStatistic {
    private long sites;
    private long pages;
    private long lemmas;
    private boolean isIndexing;

    public TotalStatistic(long sites, long pages, long lemmas, boolean isIndexing) {
        this.sites = sites;
        this.pages = pages;
        this.lemmas = lemmas;
        this.isIndexing = isIndexing;
    }
}
