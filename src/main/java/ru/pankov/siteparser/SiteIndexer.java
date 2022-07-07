package ru.pankov.siteparser;

import lombok.Data;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.pankov.dbhandler.DBHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

@Data
@Component
@Scope("prototype")
public class SiteIndexer {
    private volatile Set<String> linksSet;
    private String mainPageUrl;
    private SiteIndexerTask initTask;
    private DBHandler dbHandler;
    private ObjectProvider<SiteIndexerTask> taskObjectProvider;

    public SiteIndexer(String siteMainPageUrl) {
        linksSet = Collections.synchronizedSet(new HashSet<>());
        linksSet.add(mainPageUrl);
        mainPageUrl = siteMainPageUrl;
    }

    @Autowired
    public void setDbHandler(DBHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    @Autowired
    public void setTaskObjectProvider(ObjectProvider<SiteIndexerTask> taskObjectProvider) {
        this.taskObjectProvider = taskObjectProvider;
    }

    public void createIndex() {
        System.out.println("Indexing start");
        int siteId = dbHandler.addSite(mainPageUrl);
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(taskObjectProvider.getObject(mainPageUrl, linksSet, mainPageUrl, siteId));
        dbHandler.changeSiteStatus("INDEXED", siteId);
        System.out.println("Indexing finish");
    }
}
