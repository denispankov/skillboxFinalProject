package ru.pankov;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.OptimisticLockingFailureException;
import ru.pankov.entities.PageEntity;
import ru.pankov.entities.SiteEntity;
import ru.pankov.enums.SiteStatus;
import ru.pankov.repositories.PageRepository;
import ru.pankov.repositories.SiteRepository;
import ru.pankov.services.Transaction;
import ru.pankov.services.search.SearchService;
import ru.pankov.services.siteparser.SiteIndexerService;

import java.time.LocalDateTime;

public class CommandLineRunnerMain implements CommandLineRunner {
    @Autowired
    private ObjectProvider<SiteIndexerService> siteIndexerObjectProvider;
    @Value("${site-list}")
    private String[] siteList;
    @Autowired
    private SearchService searcher;

    private Logger logger;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    Transaction testTransaction;

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
            SiteIndexerService site = siteIndexerObjectProvider.getObject(siteUrl);
            site.createIndex();
        }
    }


    @Override
    public void run(String... args) {
        testTransaction.saveSite();
        //saveSite();
    }


    @Transactional
    public void saveSite(){
        SiteEntity siteEntity = siteRepository.findByName("http://www.playback.ru/");
        if (siteEntity == null){
            siteEntity = new SiteEntity();
            siteEntity.setSiteStatus(SiteStatus.INDEXING);
            siteEntity.setName("test");
            siteEntity.setLastError("");
            siteEntity.setUrl("test");
            siteEntity.setStatusTime(LocalDateTime.now());
        }
        siteRepository.save(siteEntity);
        PageEntity pageEntity = new PageEntity();
        pageEntity.setCode(200);
        pageEntity.setPath("test");
        pageEntity.setContent("sadfgsadg");
        pageEntity.setSiteEntity(siteEntity);

        pageRepository.save(pageEntity);

        throw new OptimisticLockingFailureException("test");
    }

}
