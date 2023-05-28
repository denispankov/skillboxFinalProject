package ru.pankov.controllers.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.pankov.pojo.api.request.SearchRequest;
import ru.pankov.pojo.api.response.SearchResponse;
import ru.pankov.pojo.search.SearchResult;
import ru.pankov.services.search.Searcher;

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
    public SearchResponse getSearchResult(@RequestBody SearchRequest req){


        String query = req.getQuery();
        String site = req.getSite();
        int offset = req.getOffset();
        int limit = req.getLimit();

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
