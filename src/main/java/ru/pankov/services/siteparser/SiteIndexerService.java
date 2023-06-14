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

            if (SiteIndexerTaskService.isInterrupted.get()){
                siteEntity.setSiteStatus(SiteStatus.FAILED);
                siteEntity.setLastError("manual stop");
                siteRepository.save(siteEntity);
            }else{
                siteEntity.setSiteStatus(SiteStatus.INDEXED);
                siteRepository.save(siteEntity);
            }

        } catch (Exception e){
            e.printStackTrace();
            logger.info(e.getMessage() + mainPageUrl);

            siteEntity.setSiteStatus(SiteStatus.FAILED);
            siteEntity.setLastError(e.getMessage());
            siteRepository.save(siteEntity);
        }
        logger.info("Indexing finish " + mainPageUrl);
    }

    public void stopIndex(){
        List<Runnable> tasks = forkJoinPool.shutdownNow();
        for(Runnable task: tasks){
            task.toString();
        }
    }

    public void indexPage(String url){
        logger.info("Indexing page start " + url);
        SiteEntity siteEntity = addSiteDB(mainPageUrl);
        SiteIndexerTaskService task =  taskObjectProvider.getObject(url, linksSet, mainPageUrl, siteEntity);
        task.indexSinglePage();
        logger.info("Indexing page finish " + url);
    }

    @Transactional
    private SiteEntity addSiteDB(String mainPageUrl){

        SiteEntity siteEntity = siteRepository.findByUrl(mainPageUrl);

        if (siteEntity == null) {
            siteEntity = new SiteEntity();
            siteEntity.setUrl(mainPageUrl);
            siteEntity.setName(mainPageUrl);
            siteEntity.setSiteStatus(SiteStatus.INDEXING);
            siteEntity.setStatusTime(LocalDateTime.now());

            SiteEntity newSiteEntity  = siteRepository.save(siteEntity);
            return newSiteEntity;
        }

        return siteEntity;
    }
}
