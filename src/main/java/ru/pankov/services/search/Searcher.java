package ru.pankov.services.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.pankov.pojo.lemmanization.Lemma;
import ru.pankov.pojo.search.SearchResult;
import ru.pankov.repositories.SiteRepository;
import ru.pankov.services.lemmanization.Lemmatizer;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class Searcher {
    Lemmatizer lemmatizer;
    @Autowired
    SiteRepository siteRepository;

    @Autowired
    public void setLemmatizer(Lemmatizer lemmatizer){
        this.lemmatizer = lemmatizer;
    }


    public List<SearchResult> search(String request, String site, int limit, int offset){
        List<Lemma> requestLemmas = lemmatizer.getLemmas(request);
        List<String> lemmaNameList = requestLemmas.stream().map(l -> l.getLemma()).collect(Collectors.toList());

        List<SearchResult> searchResults = siteRepository.search(site,lemmaNameList,offset,limit);
        return searchResults;
    }
}
