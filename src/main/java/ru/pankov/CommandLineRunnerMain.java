package ru.pankov;

import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import ru.pankov.services.search.Searcher;
import ru.pankov.services.siteparser.SiteIndexer;


public class CommandLineRunnerMain implements CommandLineRunner {
    @Autowired
    private ObjectProvider<SiteIndexer> siteIndexerObjectProvider;
    @Value("${site-list}")
    private String[] siteList;
    @Autowired
    private Searcher searcher;

    private Logger logger;

    @Autowired
    @Qualifier("logger")
    public void setLogger(Logger logger){
        this.logger = logger;
    }
    private class SiteIndexThread extends Thread {
        private String siteUrl;
        public SiteIndexThread(String siteUrl){
            super();
            this.siteUrl = siteUrl;
        }
        public void run(){
            SiteIndexer site = siteIndexerObjectProvider.getObject(siteUrl);
            site.createIndex();
        }
    }


    @Override
    public void run(String... args) {
        logger.info("All index start");
        //List<SearchResult> searchResults = searcher.search("пример случайного запроса");
        for(int i = 0; i<siteList.length; i++){
            new SiteIndexThread(siteList[i]).start();
        }
        logger.info("All index finished");
    }
}
