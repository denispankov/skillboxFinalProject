package ru.pankov.siteparser;

import org.springframework.beans.factory.annotation.Autowired;
import ru.pankov.dbhandler.DBHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

public class SiteIndexerTask extends RecursiveAction {
    private String pageLink;
    private Set<String> linksSet;
    private String mainPageURL;
    private DBHandler dbHandler = DBHandler.getInstance();

    public SiteIndexerTask(String pageLink, Set<String> linksSet, String mainPageURL) {
        this.pageLink = pageLink;
        this.linksSet = linksSet;
        this.mainPageURL = mainPageURL;
    }

    @Override
    protected void compute() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        linksSet.add(pageLink);

        if (mainPageURL.equals(pageLink)){
            System.out.println("Start parsing");
        }

        Page newPage = PageParser.parse(pageLink);
        List<String> pageLinks = newPage.getLinks();
        List<String> newPageLinks = pageLinks.stream().filter(link -> !linksSet.contains(link) & link.contains(mainPageURL)).distinct().collect(Collectors.toList());
        linksSet.addAll(newPageLinks);


        dbHandler.createPageIndex(newPage);

        List<SiteIndexerTask> taskList = new ArrayList<>();
        for (String link : newPageLinks) {
            SiteIndexerTask task = new SiteIndexerTask(link, linksSet, mainPageURL);
            task.fork();
            taskList.add(task);
        }


        if (mainPageURL.equals(pageLink)){
            for (SiteIndexerTask task : taskList) {
                task.join();
            }
            System.out.println("End parsing");
        }

    }
}
