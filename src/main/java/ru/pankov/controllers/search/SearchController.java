package ru.pankov.controllers.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.pankov.dto.api.request.SearchRequest;
import ru.pankov.dto.api.response.SearchResponse;
import ru.pankov.dto.search.SearchResult;
import ru.pankov.services.search.SearchService;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    SearchService searcher;

    @Autowired
    public void setSearcher(SearchService searcher) {
        this.searcher = searcher;
    }

    @GetMapping("/api/search")
    public SearchResponse getSearchResult(@RequestParam Map<String, String> req){


        String query = req.get("query");
        String site = req.get("site") == null ? "" : req.get("site");
        String offsetS = req.get("offset");
        String limitS = req.get("limit");

        int offset = offsetS == null ? 0 : Integer.parseInt(offsetS);
        int limit = limitS == null ? 0 : Integer.parseInt(limitS);

        SearchResponse result = new SearchResponse();

        List<SearchResult> searchResults = searcher.search(query, site, limit, offset);

        for(SearchResult res: searchResults){
            result.fromSearchResult(res);
        }

        if (searchResults.size() > 0) {
            result.setCount(searchResults.get(0).getQuantPages());
        }else{
            result.setCount(0);
        }

        if (query == ""){
            result.setError("Задан пустой поисковый запрос");
            result.setResult(false);
        }else{
            result.setError("");
            result.setResult(true);
        }


        return result;
    }

}
