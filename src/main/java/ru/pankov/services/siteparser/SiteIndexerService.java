package ru.pankov.services.siteparser;

import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.Getter;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.pankov.entities.SiteEntity;
import ru.pankov.enums.SiteStatus;
import ru.pankov.repositories.SiteRepository;
import ru.pankov.services.SiteService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@Service
@Scope("prototype")
public class SiteIndexerService {
    private volatile Set<String> linksSet;
    private String mainPageUrl;
    private SiteIndexerTaskService initTask;

    private ObjectProvider<SiteIndexerTaskService> taskObjectProvider;

    private Logger logger;

    private ForkJoinPool forkJoinPool;

    private SiteEntity siteEntity;

    private boolean interrupted;

    @Autowired
    private PageIndexerService pageIndexerService;

    @Autowired
    @Qualifier("logger")
    public void setLogger(Logger logger){
        this.logger = logger;
    }

    @Autowired
    private SiteService siteService;

    public SiteIndexerService(String siteMainPageUrl) {
        linksSet = Collections.synchronizedSet(new HashSet<>());
        linksSet.add(mainPageUrl);
        mainPageUrl = siteMainPageUrl;
    }


    @Autowired
    public void setTaskObjectProvider(ObjectProvider<SiteIndexerTaskService> taskObjectProvider) {
        this.taskObjectProvider = taskObjectProvider;
    }

    public void createIndex() {
        logger.info("Indexing start" + mainPageUrl);

        interrupted = false;
        siteEntity = siteService.addSiteDB(mainPageUrl);

        forkJoinPool = new ForkJoinPool(16);
        try {
            forkJoinPool.invoke(taskObjectProvider.getObject(mainPageUrl, linksSet, this));

            siteService.changeSiteStatus(siteEntity, SiteStatus.INDEXED, "");

        } catch (Exception e){
            logger.info(e.getMessage() + mainPageUrl);
            interrupted = true;
            siteService.changeSiteStatus(siteEntity, SiteStatus.FAILED, e.getMessage());
        } catch (Error error){
            logger.info(error.getMessage() + mainPageUrl);
            interrupted = true;
            siteService.changeSiteStatus(siteEntity, SiteStatus.FAILED, "critical error");
        }
        forkJoinPool.shutdown();
        logger.info("Indexing finish " + mainPageUrl);
    }


    public void indexPage(String url){
        logger.info("Indexing page start " + url);
        SiteEntity siteEntity = siteService.addSiteDB(mainPageUrl);
        pageIndexerService.indexPage(url, siteEntity, true);
        logger.info("Indexing page finish " + url);
    }
    public void interruptIndexing(){
        forkJoinPool.shutdownNow();
        try {
            forkJoinPool.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
