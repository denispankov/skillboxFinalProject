package ru.pankov.dto.interfaces;

public interface SearchResultInterface {
    String getSite();
    String getSiteName();
    String getUri();
    double getRelevance();
    int getQuantPages();
    String getContent();
}
