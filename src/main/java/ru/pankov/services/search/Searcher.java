package ru.pankov.services.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.pankov.dbhandler.DBHandler;
import ru.pankov.pojo.lemmanization.Lemma;
import ru.pankov.pojo.search.SearchResult;
import ru.pankov.services.lemmanization.Lemmatizer;

import java.util.List;

@Service
public class Searcher {
    Lemmatizer lemmatizer;
    DBHandler dbHandler;

    @Autowired
    public void setLemmatizer(Lemmatizer lemmatizer){
        this.lemmatizer = lemmatizer;
    }

    @Autowired
    public void setDbHandler(DBHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    public List<SearchResult> search(String request, String site, int limit, int offset){
        List<Lemma> requestLemmas = lemmatizer.getLemmas(request);
        List<SearchResult> searchResults = dbHandler.search(requestLemmas, site, limit, offset);
        return searchResults;
    }
}
