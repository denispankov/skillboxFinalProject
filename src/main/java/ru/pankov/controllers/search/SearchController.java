package ru.pankov.controllers.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.pankov.dto.api.response.SearchResponse;
import ru.pankov.services.SearchService;
import ru.pankov.services.impl.SearchServiceImpl;

import java.util.Map;

@RestController
public class SearchController {

    @Autowired
    SearchService searcher;

    @GetMapping("/api/search")
    public SearchResponse getSearchResult(@RequestParam Map<String, String> req){

        return searcher.search(req);
    }

}
