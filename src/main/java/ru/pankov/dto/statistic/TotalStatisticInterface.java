package ru.pankov.dto.statistic;

public interface TotalStatisticInterface {
    long getSites();
    long getPages();
    long getLemmas();
    boolean getIsIndexing();
}
