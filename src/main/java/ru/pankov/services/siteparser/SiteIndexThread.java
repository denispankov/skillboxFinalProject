package ru.pankov.services.siteparser;

import lombok.Getter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.pankov.services.SiteService;

@Service
@Scope("prototype")
@Getter
public class SiteIndexThread extends Thread {
    private String siteUrl;

    private SiteIndexerService siteIndexerService;
    @Autowired
    private ObjectProvider<SiteIndexerService> siteIndexerObjectProvider;

    public SiteIndexThread(String siteUrl) {
        super();
        this.siteUrl = siteUrl;
    }

    public void run() {

        siteIndexerService = siteIndexerObjectProvider.getObject(siteUrl);
        siteIndexerService.createIndex();
    }
}
