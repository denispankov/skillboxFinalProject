package ru.pankov.services.siteparser;

import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import ru.pankov.entities.SiteEntity;
import ru.pankov.dto.siteparser.Page;
import ru.pankov.services.PageService;

import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class SiteIndexerTaskService extends RecursiveAction {
    private String pageLink;
    private Set<String> linksSet;
    private String mainPageURL;

    private PageParserService pageParser;
    private ObjectProvider<SiteIndexerTaskService> taskObjectProvider;
    private SiteEntity siteEntity;

    @Autowired
    private PageService pageService;

    private Logger logger;

    @Autowired
    @Qualifier("logger")
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public SiteIndexerTaskService(String pageLink, Set<String> linksSet, String mainPageURL, SiteEntity siteEntity) {
        this.pageLink = pageLink;
        this.linksSet = linksSet;
        this.mainPageURL = mainPageURL;
        this.siteEntity = siteEntity;
    }

    @Autowired
    public void setTaskObjectProvider(ObjectProvider<SiteIndexerTaskService> taskObjectProvider) {
        this.taskObjectProvider = taskObjectProvider;
    }

    @Autowired
    public void setPageParser(PageParserService pageParser) {
        this.pageParser = pageParser;
    }


    @Override
    protected void compute() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        linksSet.add(pageLink);

        if (mainPageURL.equals(pageLink)) {
            logger.info("Start parsing");
        }

        Page newPage = indexPage();

        List<String> pageLinks = newPage.getLinks();
        List<String> newPageLinks = pageLinks.stream().filter(link -> !linksSet.contains(link) & link.contains(mainPageURL)).distinct().collect(Collectors.toList());
        linksSet.addAll(newPageLinks);

        List<SiteIndexerTaskService> taskList = new ArrayList<>();
        for (String link : newPageLinks) {
            SiteIndexerTaskService task = taskObjectProvider.getObject(link, linksSet, mainPageURL, siteEntity);
            task.fork();
            taskList.add(task);
        }

        for (SiteIndexerTaskService task : taskList) {
            task.join();
        }
        if (mainPageURL.equals(pageLink)) {
            logger.info("End parsing");
        }

    }

    public Page indexPage() {
        Page newPage = pageParser.parse(pageLink);
        newPage.setSiteEntity(siteEntity);
        newPage.setRelativePageLink(pageLink.replaceAll(mainPageURL, ""));

        while (true) {
            try {

                pageService.indexPage(newPage);
            } catch (DataIntegrityViolationException exception) {
                continue;
            } catch (OptimisticLockingFailureException optimisticLockException) {
                continue;
            } catch (Exception e) {
                continue;
            }
            break;
        }

        return newPage;
    }

}
