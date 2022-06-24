package ru.pankov.siteparser;

import lombok.Data;

import java.util.List;

@Data
public class Page {
    private List<String> links;
    private int statusCode;
    private String content;
    private String pageLink;

    public Page(List<String> links, int statusCode, String content, String pageLink) {
        this.links = links;
        this.statusCode = statusCode;
        this.content = content;
        this.pageLink = pageLink;
    }
}
