package ru.pankov.dto.interfaces;

public interface SitesDetailStatisticInterface {
    String getUrl();
    String getName();
    String getStatus();
    String getStatusTime();
    String getError();
    int getPages();
    long getLemmas();
}
