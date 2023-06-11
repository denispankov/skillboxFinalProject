package ru.pankov.dto.statistic;

import lombok.Data;

@Data
public class TotalStatistic {
    private long sites;
    private long pages;
    private long lemmas;

    public TotalStatistic(long sites, long pages, long lemmas) {
        this.sites = sites;
        this.pages = pages;
        this.lemmas = lemmas;
    }
}
