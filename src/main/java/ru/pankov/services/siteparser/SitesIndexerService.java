package ru.pankov.services.siteparser;

import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.pankov.entities.SiteEntity;
import ru.pankov.enums.SiteStatus;
import ru.pankov.repositories.IndexRepository;
import ru.pankov.repositories.LemmaRepository;
import ru.pankov.repositories.PageRepository;
import ru.pankov.repositories.SiteRepository;
import ru.pankov.services.IndexService;
import ru.pankov.services.LemmaService;
import ru.pankov.services.PageService;
import ru.pankov.services.SiteService;
import ru.pankov.services.interfaces.DbCleaner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class SitesIndexerService {
    @Autowired
    @Qualifier("logger")
    private Logger logger;
    @Autowired
    private ObjectProvider<SiteIndexThread> siteIndexerThreadProvider;
    @Value("${site-list}")
    private String[] siteList;
    private List<SiteIndexThread> indexProcess = new ArrayList<>();
    public static AtomicBoolean isInterrupted = new AtomicBoolean(false);

    @Autowired
    private SiteService siteService;

    @Autowired
    List<DbCleaner> dbCleaners;

    public String indexAll() {
        boolean indexingIsRunning = false;
        String error = "";
        logger.info("All index start");


        if (siteService.indexingSiteExists()){
            indexingIsRunning = true;
        }

        if (indexingIsRunning == false) {

            dbCleaners.forEach(c -> c.deleteAll());

            for (int i = 0; i < siteList.length; i++) {
                SiteIndexThread th = siteIndexerThreadProvider.getObject(siteList[i]);
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


        if (siteService.indexingSiteExists()){
            indexingIsRunning = true;
        }

        if (indexingIsRunning == true) {

            SitesIndexerService.isInterrupted.set(true);
            siteService.interruptAllManual();

            logger.info("All index interrupted");
        } else {
            error = "Индексация не запущена";
        }

        return error;
    }
}
