package ru.pankov.restcontrollers;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.pankov.siteparser.SiteIndexer;
import ru.pankov.siteparser.SitesIndexer;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
public class ParseController {
    @Autowired
    SitesIndexer indexer;

    @GetMapping("/api/startIndexing")
    public ResponseEntity<?> startIndexing(){
        @Data
        class Result{
            private boolean result;
            private String error;
        }
        Result result = new Result();

        String error = indexer.indexAll();

        if (error != ""){
            result.setResult(false);
            result.setError(error);
        }else {
            result.setResult(true);
        }

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("/api/indexPage")
    public ResponseEntity<?> indexPage(@RequestParam Map<String, String> req){
        @Data
        class Result{
            private boolean result;
            private String error;
        }
        Result result = new Result();

        SiteIndexer indexer = new SiteIndexer(req.get("url"));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/api/stopIndexing")
    public ResponseEntity<?> stopIndexing(){
        @Data
        class Result{
            private boolean result;
            private String error;
        }
        Result result = new Result();

        String error = indexer.stopAll();

        if (error != ""){
            result.setResult(false);
            result.setError(error);
        }else {
            result.setResult(true);
        }

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/api/statistics")
    public ResponseEntity<?> getStatistic(){
        @Data
        class Result{
            @Data
            class Total{
                private long sites;
                private long pages;
                private long lemmas;
                private boolean isIndexing;

                public Total(long sites, long pages, long lemmas, boolean isIndexing) {
                    this.sites = sites;
                    this.pages = pages;
                    this.lemmas = lemmas;
                    this.isIndexing = isIndexing;
                }
            }
            @Data
            class SiteDet{
                private String url;
                private String name;
                private String status;
                private long statusTime;
                private String error;
                private int pages;
                private long lemmas;

                public SiteDet(String url, String name, String status, int statusTime, String error, int pages, long lemmas) {
                    this.url = url;
                    this.name = name;
                    this.status = status;
                    this.statusTime = statusTime;
                    this.error = error;
                    this.pages = pages;
                    this.lemmas = lemmas;
                }
            }
            @Data
            class Statistic{
                public Statistic(){
                    total = null;
                    detailed = new ArrayList<>();
                }
                public void addDetail(SiteDet det){
                    detailed.add(det);
                }

                private Total total;
                private List<SiteDet> detailed;
            }
            private boolean result;
            private Statistic statistics;

            public Result(){
                statistics = new Statistic();
            }

            public void setTotal(long sites, long pages, long lemmas, boolean isIndexing){
                statistics.setTotal(new Total(sites, pages, lemmas, isIndexing));
            }

            public void addDetail(String url, String name, String status, int statusTime, String error, int pages, long lemmas){
                statistics.addDetail(new SiteDet(url, name, status, statusTime, error, pages, lemmas));
            }
        }
        Result result = new Result();
        result.setResult(true);
        /* Заглушка*/
        result.setTotal(1,2,3,true);
        result.addDetail("test1", "test1", "1",1,"",1,1);
        result.addDetail("test2", "test2", "2",2,"",2,2);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
