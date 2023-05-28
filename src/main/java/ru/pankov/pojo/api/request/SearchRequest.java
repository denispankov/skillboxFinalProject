package ru.pankov.pojo.api.request;

import lombok.Data;

@Data
public class SearchRequest {
    private String query;
    private String site;
    private int offset;
    private int limit;
}
