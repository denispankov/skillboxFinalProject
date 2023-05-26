package ru.pankov.controllers.parse;

import lombok.Data;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.pankov.dbhandler.DBHandler;
import ru.pankov.dbhandler.ResultStatistic;
import ru.pankov.services.siteparser.SiteIndexer;
import ru.pankov.services.siteparser.SitesIndexer;


import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
public class ParseController {
    @Autowired
    SitesIndexer indexer;

    DBHandler dbHandler;

    @Autowired
    public void setDbHandler(DBHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    @Autowired
    private ObjectProvider<SiteIndexer> siteIndexerObjectProvider;

    @Value("${site-list}")
    private String[] siteList;

    @GetMapping("/api/startIndexing")
    public ResponseEntity<?> startIndexing() {
        @Data
        class Result {
            private boolean result;
            private String error;
        }
        Result result = new Result();

        String error = indexer.indexAll();

        if (error != "") {
            result.setResult(false);
            result.setError(error);
        } else {
            result.setResult(true);
        }

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("/api/indexPage")
    public ResponseEntity<?> indexPage(@RequestParam Map<String, String> req) {
        @Data
        class Result {
            private boolean result;
            private String error;
        }
        Result result = new Result();
        String site = "";
        String url = req.get("url");
        String regexp = "^((http)|(https))://[a-z]*.[a-z]*.[a-z]{2}/";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            site = matcher.group(0);
        }
        if (Arrays.asList(siteList).contains(site)) {
            SiteIndexer indexer = siteIndexerObjectProvider.getObject(site);
            indexer.indexPage(url);
            result.setResult(true);
        } else {
            result.setResult(false);
            result.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/api/stopIndexing")
    public ResponseEntity<?> stopIndexing() {
        @Data
        class Result {
            private boolean result;
            private String error;
        }
        Result result = new Result();

        String error = indexer.stopAll();

        if (error != "") {
            result.setResult(false);
            result.setError(error);
        } else {
            result.setResult(true);
        }

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/api/statistics")
    public ResponseEntity<?> getStatistic() {

        ResultStatistic result = dbHandler.getStatistic();
        result.setResult(true);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
