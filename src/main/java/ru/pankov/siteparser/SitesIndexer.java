package ru.pankov.siteparser;

import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.pankov.dbhandler.DBHandler;

import java.util.ArrayList;
import java.util.List;

@Component
public class SitesIndexer {
    private Logger logger;
    @Autowired
    private ObjectProvider<SiteIndexer> siteIndexerObjectProvider;
    @Value("${site-list}")
    private String[] siteList;
    private List<SiteIndexThread> indexProcess = new ArrayList<>();

    DBHandler dbHandler;

    @Autowired
    public void setDbHandler(DBHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    @Autowired
    @Qualifier("logger")
    public void setLogger(Logger logger){
        this.logger = logger;
    }

    private class SiteIndexThread extends Thread {
        private String siteUrl;
        private SiteIndexer site;

        public SiteIndexThread(String siteUrl) {
            super();
            this.siteUrl = siteUrl;
        }

        public void run() {
            site = siteIndexerObjectProvider.getObject(siteUrl);
            site.createIndex();
        }

        public void stopIndex(){
            site.stopIndex();
        }
    }

    public String indexAll() {
        boolean indexingIsRunning = false;
        String error = "";
        logger.info("All index start");

        for (SiteIndexThread th : indexProcess) {
            if (th.isAlive()) {
                indexingIsRunning = true;
            }
        }

        if (indexingIsRunning == false) {
            for (int i = 0; i < siteList.length; i++) {
                SiteIndexThread th = new SiteIndexThread(siteList[i]);
                th.start();
                indexProcess.add(th);
            }
            logger.info("All index finished");
        } else {
            error = "Индексация уже запущена";
        }

        return error;
    }

    public String stopAll(){
        boolean indexingIsRunning = false;
        String error = "";

        for (SiteIndexThread th : indexProcess) {
            if (th.isAlive()) {
                indexingIsRunning = true;
            }
        }

        if (indexingIsRunning == true) {

            for (SiteIndexThread th : indexProcess) {
                th.stopIndex();
            }
            dbHandler.clearQueue();
            logger.info("All index stoped");
        } else {
            error = "Индексация не запущена";
        }

        return error;
    }


}