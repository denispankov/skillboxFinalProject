package ru.pankov.dto.interfaces;

public interface TotalStatisticInterface {
    long getSites();
    long getPages();
    long getLemmas();
    boolean getIsIndexing();
}
