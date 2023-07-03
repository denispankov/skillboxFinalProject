package ru.pankov.controllers.index;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.pankov.dto.api.request.IndexRequest;
import ru.pankov.dto.api.response.IndexResponse;
import ru.pankov.services.IndexService;
import ru.pankov.services.impl.IndexServiceImpl;
import ru.pankov.siteIndexer.SiteIndexer;


@RestController
public class SiteIndexController {
    @Autowired
    IndexService indexService;

    @Autowired
    private ObjectProvider<SiteIndexer> siteIndexerObjectProvider;

    @GetMapping("/api/startIndexing")
    public IndexResponse startIndexing() {

        return indexService.indexAll();
    }

    @PostMapping(value = "/api/indexPage", consumes = {"application/x-www-form-urlencoded; charset=UTF-8"})
    public IndexResponse indexPage(IndexRequest req) {

        return indexService.indexSinglePage(req);
    }

    @GetMapping("/api/stopIndexing")
    public IndexResponse stopIndexing() {

        return indexService.stopAll();
    }
}
