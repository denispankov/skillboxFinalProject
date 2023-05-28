package ru.pankov.services.siteparser;

import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.pankov.entities.SiteEntity;
import ru.pankov.pojo.statistic.ResultStatistic;
import ru.pankov.repositories.IndexRepository;
import ru.pankov.repositories.LemmaRepository;
import ru.pankov.repositories.PageRepository;
import ru.pankov.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class SitesIndexerService {
    private Logger logger;
    @Autowired
    private ObjectProvider<SiteIndexerService> siteIndexerObjectProvider;
    @Value("${site-list}")
    private String[] siteList;
    private List<SiteIndexThread> indexProcess = new ArrayList<>();

    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    @Autowired
    private SiteRepository siteRepository;


    @Autowired
    @Qualifier("logger")
    public void setLogger(Logger logger){
        this.logger = logger;
    }

    private class SiteIndexThread extends Thread {
        private String siteUrl;
        private SiteIndexerService site;

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

        List<SiteEntity> siteEntities = siteRepository.findBySiteStatus(1);

        if (!siteEntities.isEmpty()){
            indexingIsRunning = true;
        }

        if (indexingIsRunning == false) {

            pageRepository.deleteAll();
            lemmaRepository.deleteAll();
            indexRepository.deleteAll();
            siteRepository.deleteAll();

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

        /*toDO
        переделать на энум, может не работать этот кусок кода, не понятно что возвращается, когда не найдены строки
         */
        List<SiteEntity> siteEntities = siteRepository.findBySiteStatus(1);

        if (!siteEntities.isEmpty()){
            indexingIsRunning = true;
        }

        if (indexingIsRunning == true) {

            for (SiteIndexThread th : indexProcess) {
                th.stopIndex();
            }
            siteEntities = siteRepository.findBySiteStatus(1);
            for (SiteEntity siteEntity: siteEntities){
                siteEntity.setSiteStatus(3);
                siteRepository.save(siteEntity);
            }

            logger.info("All index stoped");
        } else {
            error = "Индексация не запущена";
        }

        return error;
    }

    public ResultStatistic getStatistic(){
        return new ResultStatistic(siteRepository.getStatisticTotal(), siteRepository.getStatisticDetail());
    }


}
