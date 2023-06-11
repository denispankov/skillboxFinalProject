package ru.pankov.dto.search;

public interface SearchResultInterface {
    String getSite();
    String getSiteName();
    String getUri();
    double getRelevance();
    int getQuantPages();
    String getContent();
}
