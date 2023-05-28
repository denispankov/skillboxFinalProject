package ru.pankov.pojo.api.response;

import lombok.Data;
import ru.pankov.pojo.search.SearchResult;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResponse {
    @lombok.Data
    class Data{
        private String site;
        private String siteName;
        private String uri;
        private String title;
        private String snippet;
        private double relevance;

        public Data(String site, String sateName, String uri, String title, String snippet, double relevance) {
            this.site = site;
            this.siteName = sateName;
            this.uri = uri;
            this.title = title;
            this.snippet = snippet;
            this.relevance = relevance;
        }

    }
    public SearchResponse(){
        data = new ArrayList<>();
    }
    public void fromSearchResult(SearchResult searchResult){
        data.add(new Data(searchResult.getSite(),
                searchResult.getSiteName(),
                searchResult.getUri(),
                searchResult.getTitle(),
                searchResult.getSnippet(),
                searchResult.getRelevance())
        );
    }
    private boolean result;
    private int count;
    private String error;
    private List<Data> data;
}
