package ru.pankov.services.siteparser;

import jakarta.transaction.Transactional;
import lombok.Data;
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

        SiteEntity siteEntity = siteService.addSiteDB(mainPageUrl);

        forkJoinPool = new ForkJoinPool();
        try {
            forkJoinPool.invoke(taskObjectProvider.getObject(mainPageUrl, linksSet, siteEntity));

            if (SitesIndexerService.isInterrupted.get()){
                siteService.changeSiteStatus(siteEntity, SiteStatus.FAILED, "manual stop");
            }else{
                if(siteService.getSiteStatus(siteEntity) == SiteStatus.INDEXING) {
                    siteService.changeSiteStatus(siteEntity, SiteStatus.INDEXED, "");
                }
            }

        } catch (Exception e){
            e.printStackTrace();
            logger.info(e.getMessage() + mainPageUrl);

            siteService.changeSiteStatus(siteEntity, SiteStatus.FAILED, e.getMessage());
        }
        logger.info("Indexing finish " + mainPageUrl);
    }


    public void indexPage(String url){
        logger.info("Indexing page start " + url);
        SiteEntity siteEntity = siteService.addSiteDB(mainPageUrl);
        pageIndexerService.indexPage(url, siteEntity, true);
        logger.info("Indexing page finish " + url);
    }
}
