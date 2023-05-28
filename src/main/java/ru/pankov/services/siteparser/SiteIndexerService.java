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
import ru.pankov.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
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

    @Autowired
    @Qualifier("logger")
    public void setLogger(Logger logger){
        this.logger = logger;
    }

    @Autowired
    SiteRepository siteRepository;

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

        SiteEntity siteEntity = addSiteDB(mainPageUrl);

        forkJoinPool = new ForkJoinPool();
        try {
            forkJoinPool.invoke(taskObjectProvider.getObject(mainPageUrl, linksSet, mainPageUrl, siteEntity));
            siteEntity.setSiteStatus(2);
            siteRepository.save(siteEntity);
        }catch (CancellationException ce){
            siteEntity.setSiteStatus(3);
            siteEntity.setLastError("manual stop");
            siteRepository.save(siteEntity);
        }
        catch (Exception e){
            e.printStackTrace();
            logger.info(e.getMessage() + mainPageUrl);

            siteEntity.setSiteStatus(3);
            siteEntity.setLastError(e.getMessage());
            siteRepository.save(siteEntity);
        }
        logger.info("Indexing finish " + mainPageUrl);
    }

    public void stopIndex(){
        forkJoinPool.shutdownNow();
    }

    @Transactional
    public void indexPage(String url){
        logger.info("Indexing page start " + url);
        SiteEntity siteEntity = addSiteDB(mainPageUrl);
        SiteIndexerTaskService task =  taskObjectProvider.getObject(url, linksSet, mainPageUrl, siteEntity);
        task.indexPage();
        logger.info("Indexing page finish " + url);
    }

    @Transactional
    private SiteEntity addSiteDB(String mainPageUrl){
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setUrl(mainPageUrl);
        siteEntity.setName(mainPageUrl);
        siteEntity.setSiteStatus(1);
        siteEntity.setStatusTime(LocalDateTime.now());

        SiteEntity newSiteEntity  = siteRepository.save(siteEntity);
        return newSiteEntity;
    }
}
