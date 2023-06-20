package ru.pankov.services.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.pankov.dto.lemmanization.Lemma;
import ru.pankov.dto.search.SearchResult;
import ru.pankov.dto.interfaces.SearchResultInterface;
import ru.pankov.repositories.SiteRepository;
import ru.pankov.services.lemmanization.LemmatizerService;
import ru.pankov.services.siteparser.PageParserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    LemmatizerService lemmatizer;
    @Autowired
    SiteRepository siteRepository;

    @Autowired
    private PageParserService pageParserService;


    public List<SearchResult> search(String request, String site, int limit, int offset){
        List<Lemma> requestLemmas = lemmatizer.getLemmas(request);
        List<String> lemmaNameList = requestLemmas.stream().map(l -> l.getLemma()).collect(Collectors.toList());

        List<SearchResultInterface> searchResults = siteRepository.searchResult(site,lemmaNameList,offset,limit);

        List<SearchResult> results = new ArrayList<>();

        for(SearchResultInterface searchResult: searchResults){
            SearchResult searchResultTemp = new SearchResult(searchResult.getUri(),
                    pageParserService.getHTMLTitle(searchResult.getContent()),
                    pageParserService.getHTMLSnippet(searchResult.getContent(), requestLemmas),
                    searchResult.getRelevance(),
                    searchResult.getQuantPages(),
                    searchResult.getSiteName(),
                    searchResult.getSite());
            results.add(searchResultTemp);
        }
        return results;
    }
}
