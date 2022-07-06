package ru.pankov.siteparser;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.pankov.dbhandler.DBHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class SiteIndexerTask extends RecursiveAction {
    private String pageLink;
    private Set<String> linksSet;
    private String mainPageURL;
    private DBHandler dbHandler;
    private PageParser pageParser;
    private ObjectProvider<SiteIndexerTask> taskObjectProvider;

    public SiteIndexerTask(String pageLink, Set<String> linksSet, String mainPageURL) {
        this.pageLink = pageLink;
        this.linksSet = linksSet;
        this.mainPageURL = mainPageURL;
    }

    @Autowired
    public void setTaskObjectProvider(ObjectProvider<SiteIndexerTask> taskObjectProvider) {
        this.taskObjectProvider = taskObjectProvider;
    }

    @Autowired
    public void setPageParser(PageParser pageParser) {
        this.pageParser = pageParser;
    }

    @Autowired
    public void setDbHandler(DBHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    @Override
    protected void compute() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        linksSet.add(pageLink);

        if (mainPageURL.equals(pageLink)){
            System.out.println("Start parsing");
        }

        Page newPage = pageParser.parse(pageLink);
        List<String> pageLinks = newPage.getLinks();
        List<String> newPageLinks = pageLinks.stream().filter(link -> !linksSet.contains(link) & link.contains(mainPageURL)).distinct().collect(Collectors.toList());
        linksSet.addAll(newPageLinks);


        dbHandler.createPageIndex(newPage);

        List<SiteIndexerTask> taskList = new ArrayList<>();
        for (String link : newPageLinks) {
            SiteIndexerTask task =  taskObjectProvider.getObject(link, linksSet, mainPageURL);
            task.fork();
            taskList.add(task);
        }

        for (SiteIndexerTask task : taskList) {
            task.join();
        }
        if (mainPageURL.equals(pageLink)){
            System.out.println("End parsing");
        }

    }
}
