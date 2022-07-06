package ru.pankov.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.pankov.dbhandler.DBHandler;
import ru.pankov.lemmanization.Lemma;
import ru.pankov.lemmanization.Lemmatizer;

import java.util.List;

@Component
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

    public List<SearchResult> search(String request){
        List<Lemma> requestLemmas = lemmatizer.getLemmas(request);
        List<SearchResult> searchResults = dbHandler.search(requestLemmas);
        return searchResults;
    }
}
