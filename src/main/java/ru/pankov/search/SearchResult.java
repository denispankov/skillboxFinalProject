package ru.pankov.search;

import lombok.Data;

@Data
public class SearchResult {
    private String uri;
    private String title;
    private String snippet;
    private double relevance;

    public SearchResult(String uri, String title, String snippet, double relevance) {
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
    }
}
