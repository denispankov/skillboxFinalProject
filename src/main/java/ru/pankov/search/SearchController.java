package ru.pankov.search;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class SearchController {


    Searcher searcher;

    @Autowired
    public void setSearcher(Searcher searcher) {
        this.searcher = searcher;
    }

    @GetMapping("/api/search")
    public ResponseEntity<?> getSearchResult(@RequestParam Map<String, String> req){
        @Data
        class Result {
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
            public Result(){
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

        String query = req.get("query");
        String site = req.get("site");
        int offset = Integer.parseInt(req.get("offset"));
        int limit = Integer.parseInt(req.get("limit"));

        Result result = new Result();

        List<SearchResult> searchResults = searcher.search(query, site, limit, offset);

        for(SearchResult res: searchResults){
            result.fromSearchResult(res);
        }

        if (searchResults.size() > 0) {
            result.count = searchResults.get(0).getQuantPages();
        }else{
            result.count = 0;
        }

        if (query == ""){
            result.setError("Задан пустой поисковый запрос");
            result.setResult(false);
        }else{
            result.setError("");
            result.setResult(true);
        }


        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
