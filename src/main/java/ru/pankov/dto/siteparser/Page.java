package ru.pankov.dto.siteparser;

import lombok.Data;
import ru.pankov.entities.SiteEntity;

import java.util.List;

@Data
public class Page {
    private List<String> links;
    private int statusCode;
    private String content;
    private String pageLink;
    private String contentText;
    private String titleText;
    private SiteEntity siteEntity;
    private String relativePageLink;

    public Page(List<String> links, int statusCode, String content, String pageLink, String contentLemmas, String titleLemmas) {
        this.links = links;
        this.statusCode = statusCode;
        this.content = content;
        this.pageLink = pageLink;
        this.contentText = contentLemmas;
        this.titleText = titleLemmas;
    }
}
