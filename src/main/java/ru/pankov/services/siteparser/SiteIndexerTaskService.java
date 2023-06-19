package ru.pankov.services.siteparser;

import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.pankov.entities.SiteEntity;
import ru.pankov.dto.siteparser.Page;

import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class SiteIndexerTaskService extends RecursiveAction {
    private String pageLink;
    private Set<String> linksSet;
    private SiteEntity siteEntity;

    private final int DELAY_MILLISECONDS = 500;

    @Autowired
    private ObjectProvider<SiteIndexerTaskService> taskObjectProvider;

    @Autowired
    @Qualifier("logger")
    private Logger logger;

    @Autowired
    private PageIndexerService pageIndexerService;


    public SiteIndexerTaskService(String pageLink, Set<String> linksSet, SiteEntity siteEntity) {
        this.pageLink = pageLink;
        this.linksSet = linksSet;
        this.siteEntity = siteEntity;
    }


    @Override
    protected void compute() {
        if (SitesIndexerService.isInterrupted.get()){
            return;
        }
        try {
            Thread.sleep(DELAY_MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        linksSet.add(pageLink);

        if (siteEntity.getUrl().equals(pageLink)) {
            logger.info("Start parsing");
        }

        Page newPage = pageIndexerService.indexPage(pageLink, siteEntity, false);

        List<String> pageLinks = newPage.getLinks();
        List<String> newPageLinks = pageLinks.stream().filter(link -> !linksSet.contains(link) & link.contains(siteEntity.getUrl())).distinct().collect(Collectors.toList());
        linksSet.addAll(newPageLinks);

        List<SiteIndexerTaskService> taskList = new ArrayList<>();
        for (String link : newPageLinks) {
            SiteIndexerTaskService task = taskObjectProvider.getObject(link, linksSet, siteEntity.getUrl(), siteEntity);
            task.fork();
            taskList.add(task);
        }

        for (SiteIndexerTaskService task : taskList) {
            task.join();
        }
        if (siteEntity.getUrl().equals(pageLink)) {
            logger.info("End parsing");
        }

    }

}
