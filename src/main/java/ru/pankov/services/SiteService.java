package ru.pankov.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.pankov.entities.SiteEntity;
import ru.pankov.enums.SiteStatus;
import ru.pankov.repositories.SiteRepository;
import ru.pankov.services.interfaces.DbCleaner;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SiteService implements DbCleaner {
    @Autowired
    SiteRepository siteRepository;

    @Transactional
    public SiteEntity addSiteDB(String mainPageUrl){

        SiteEntity siteEntity = siteRepository.findByUrl(mainPageUrl);

        if (siteEntity == null) {
            siteEntity = new SiteEntity();
            siteEntity.setUrl(mainPageUrl);
            siteEntity.setName(mainPageUrl);
            siteEntity.setSiteStatus(SiteStatus.INDEXING);
            siteEntity.setStatusTime(LocalDateTime.now());

            SiteEntity newSiteEntity  = siteRepository.save(siteEntity);
            return newSiteEntity;
        }

        return siteEntity;
    }

    public void changeSiteStatus(SiteEntity siteEntity, SiteStatus siteStatus, String error){
        siteEntity.setSiteStatus(siteStatus);
        siteEntity.setLastError(error);
        siteRepository.save(siteEntity);
    }

    public void deleteAll(){
        siteRepository.deleteAllWithQuery();
    }

    public boolean indexingSiteExists(){
        List<SiteEntity> siteEntities = siteRepository.findBySiteStatus(SiteStatus.INDEXING);

        return !siteEntities.isEmpty();
    }

    public SiteStatus getSiteStatus(SiteEntity siteEntity){
        return siteRepository.findByName(siteEntity.getName()).getSiteStatus();
    }

    public SiteEntity getSiteByUrl(String url){
        return siteRepository.findByUrl(url);
    }

    public void interruptAllManual(){
        List<SiteEntity> siteEntities = siteRepository.findBySiteStatus(SiteStatus.INDEXING);
        siteEntities.forEach(e -> changeSiteStatus(e, SiteStatus.FAILED, "manual stop"));
    }
}
