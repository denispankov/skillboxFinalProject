package ru.pankov.siteparser;

import lombok.Data;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private Logger logger;

    private ForkJoinPool forkJoinPool;
    private int siteId;

    @Autowired
    @Qualifier("logger")
    public void setLogger(Logger logger){
        this.logger = logger;
    }

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
        logger.info("Indexing start" + mainPageUrl);
        siteId = dbHandler.addSite(mainPageUrl);
        forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(taskObjectProvider.getObject(mainPageUrl, linksSet, mainPageUrl, siteId));
        dbHandler.changeSiteStatus("INDEXED", siteId);
        logger.info("Indexing finish " + mainPageUrl);
    }

    public void stopIndex(){
        forkJoinPool.shutdownNow();
    }

    public void indexPage(String url){
        logger.info("Indexing page start " + url);
        siteId = dbHandler.addSite(mainPageUrl);
        SiteIndexerTask task =  taskObjectProvider.getObject(url, linksSet, mainPageUrl, siteId);
        task.indexPage();
        logger.info("Indexing page finish " + url);
    }
}
