package ru.pankov.siteparser;

import lombok.Data;

import java.util.List;

@Data
public class Page {
    private List<String> links;
    private int statusCode;
    private String content;

    public Page(List<String> links, int statusCode, String content) {
        this.links = links;
        this.statusCode = statusCode;
        this.content = content;
    }
}
