package ru.pankov.siteparser;

import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

@Data
public class SiteIndexer {
    private Set<String> linksSet;
    private String mainPageUrl;
    private SiteIndexerTask initTask;

    public SiteIndexer(String siteMainPageUrl) {
        linksSet = Collections.synchronizedSet(new HashSet<>());
        linksSet.add(mainPageUrl);
        mainPageUrl = siteMainPageUrl;
    }

    public void createIndex() {
        new ForkJoinPool().invoke(new SiteIndexerTask(mainPageUrl, linksSet, mainPageUrl));
        System.out.println("Parse ended");
    }
}
