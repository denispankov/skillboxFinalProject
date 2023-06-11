package ru.pankov.controllers.parse;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import ru.pankov.dto.api.request.IndexRequest;
import ru.pankov.dto.statistic.ResultStatistic;
import ru.pankov.dto.api.response.IndexResponse;
import ru.pankov.services.siteparser.SiteIndexerService;
import ru.pankov.services.siteparser.SitesIndexerService;


import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
public class SiteIndexController {
    @Autowired
    SitesIndexerService indexerService;

    @Autowired
    private ObjectProvider<SiteIndexerService> siteIndexerObjectProvider;

    @Value("${site-list}")
    private String[] siteList;

    @GetMapping("/api/startIndexing")
    public IndexResponse startIndexing() {
        IndexResponse result = new IndexResponse();

        String error = indexerService.indexAll();

        if (error != "") {
            result.setResult(false);
            result.setError(error);
        } else {
            result.setResult(true);
        }

        return result;
    }

    @PostMapping("/api/indexPage")
    public IndexResponse indexPage(@RequestBody IndexRequest req) {

        IndexResponse result = new IndexResponse();
        String site = "";
        String url = req.getUrl();
        String regexp = "^((http)|(https))://[a-z]*.[a-z]*.[a-z]{2}/";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            site = matcher.group(0);
        }
        if (Arrays.asList(siteList).contains(site)) {
            SiteIndexerService indexer = siteIndexerObjectProvider.getObject(site);
            indexer.indexPage(url);
            result.setResult(true);
        } else {
            result.setResult(false);
            result.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }

        return result;
    }

    @GetMapping("/api/stopIndexing")
    public IndexResponse stopIndexing() {
        IndexResponse result = new IndexResponse();

        String error = indexerService.stopAll();

        if (error != "") {
            result.setResult(false);
            result.setError(error);
        } else {
            result.setResult(true);
        }

        return result;
    }

    @GetMapping("/api/statistics")
    public ResultStatistic getStatistic() {

        ResultStatistic result = indexerService.getStatistic();;
        result.setResult(true);
        return result;
    }
}
