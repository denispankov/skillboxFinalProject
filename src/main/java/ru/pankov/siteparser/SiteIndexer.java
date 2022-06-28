package ru.pankov.siteparser;

import lombok.Data;
import ru.pankov.dbhandler.DBHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

@Data
public class SiteIndexer {
    private Set<String> linksSet;
    private String mainPageUrl;
    private SiteIndexerTask initTask;
    private DBHandler dbHandler = DBHandler.getInstance();

    public SiteIndexer(String siteMainPageUrl) {
        linksSet = Collections.synchronizedSet(new HashSet<>());
        linksSet.add(mainPageUrl);
        mainPageUrl = siteMainPageUrl;
    }

    public void createIndex() {
        System.out.println("Indexing start");
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(new SiteIndexerTask(mainPageUrl, linksSet, mainPageUrl));
        dbHandler.shutdownMainThread();
        while (dbHandler.isMainThreadRunning()){

        }
        System.out.println("Indexing finish");
    }
}
