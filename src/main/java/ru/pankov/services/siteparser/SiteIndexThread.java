package ru.pankov.services.siteparser;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.pankov.entities.SiteEntity;
import ru.pankov.enums.SiteStatus;
import ru.pankov.services.SiteService;

@Service
@Scope("prototype")
public class SiteIndexThread extends Thread{
    private String siteUrl;

    @Autowired
    private SiteService siteService;
    @Autowired
    private ObjectProvider<SiteIndexerService> siteIndexerObjectProvider;

    public SiteIndexThread(String siteUrl) {
        super();
        this.siteUrl = siteUrl;
    }

    public void run() {
        try {
            siteIndexerObjectProvider.getObject(siteUrl).createIndex();
        }catch (Exception e){
            SiteEntity siteEntity = siteService.getSiteByUrl(siteUrl);
            siteService.changeSiteStatus(siteEntity, SiteStatus.FAILED, e.getMessage());
        }
    }
}
