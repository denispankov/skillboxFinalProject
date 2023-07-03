package ru.pankov.siteIndexer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.RecursiveAction;

@Service
@Scope("prototype")
public class SiteIndexerTask extends RecursiveAction{
    private String pageLink;

    private final int DELAY_MILLISECONDS = 150;

    @Autowired
    private ObjectProvider<SiteIndexerTask> taskObjectProvider;

    private SiteIndexer siteIndexer;


    public SiteIndexerTask(String pageLink, SiteIndexer siteIndexer) {
        this.pageLink = pageLink;
        this.siteIndexer = siteIndexer;
    }


    @Override
    protected void compute(){
        if (siteIndexer.isInterrupted()) {
            return;
        }
        try {
            Thread.sleep(DELAY_MILLISECONDS);
        } catch (InterruptedException e) {
        }

        List<String> newPageLinks = siteIndexer.indexPage(pageLink, siteIndexer.getSiteEntity());


        List<SiteIndexerTask> taskList = new ArrayList<>();
        for (String link : newPageLinks) {
            SiteIndexerTask task = taskObjectProvider.getObject(link, siteIndexer);
            task.fork();
            taskList.add(task);
        }

        for (SiteIndexerTask task : taskList) {
            task.join();
        }

    }

}
