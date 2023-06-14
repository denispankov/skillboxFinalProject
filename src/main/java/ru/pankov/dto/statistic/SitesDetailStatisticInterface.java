package ru.pankov.dto.statistic;

public interface SitesDetailStatisticInterface {
    String getUrl();
    String getName();
    String getStatus();
    String getStatusTime();
    String getError();
    int getPages();
    long getLemmas();
}
