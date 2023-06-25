package ru.pankov;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import ru.pankov.services.SiteService;


public class TerminateBean {
    @Autowired
    private SiteService siteService;

    @PreDestroy
    public void onDestroy(){
        siteService.interruptAllManual("App closed");
    }
}
