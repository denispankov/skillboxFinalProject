package ru.pankov.dto.search;

import lombok.Data;

@Data
public class SearchResult{
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private double relevance;
    private int quantPages;
    private String content;

    public SearchResult(String uri, String title, String snippet, double relevance, int quantPages, String site, String siteName) {
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
        this.quantPages = quantPages;
        this.site = site;
        this.siteName = siteName;
    }

    public SearchResult(String uri, String content, double relevance, int quantPages, String site, String siteName) {
        this.uri = uri;
        this.content = content;
        this.relevance = relevance;
        this.quantPages = quantPages;
        this.site = site;
        this.siteName = siteName;
    }
}
