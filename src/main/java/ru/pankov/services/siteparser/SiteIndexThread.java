package ru.pankov.services.siteparser;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class SiteIndexThread extends Thread{
    private String siteUrl;

    @Autowired
    private ObjectProvider<SiteIndexerService> siteIndexerObjectProvider;

    public SiteIndexThread(String siteUrl) {
        super();
        this.siteUrl = siteUrl;
    }

    public void run() {
        siteIndexerObjectProvider.getObject(siteUrl).createIndex();
    }
}
