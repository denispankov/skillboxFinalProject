package ru.pankov.search;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {


    @GetMapping("/api/search")
    public ResponseEntity<?> getSearchResult(@RequestParam Map<String, String> req){
        @Data
        class Result {
            @lombok.Data
            class Data{
                private String site;
                private String sateName;
                private String uri;
                private String title;
                private String snippet;
                private float relevance;

                public Data(String site, String sateName, String uri, String title, String snippet, float relevance) {
                    this.site = site;
                    this.sateName = sateName;
                    this.uri = uri;
                    this.title = title;
                    this.snippet = snippet;
                    this.relevance = relevance;
                }
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

        if (query == ""){
            result.setError("Задан пустой поисковый запрос");
            result.setResult(false);
        }else{
            result.setError("");
            result.setResult(false);
        }

        /*todo Добавить в поисковой sql запрос параметры site, offset, limit*/
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
