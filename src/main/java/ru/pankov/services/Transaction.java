package ru.pankov.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import ru.pankov.entities.PageEntity;
import ru.pankov.entities.SiteEntity;
import ru.pankov.enums.SiteStatus;
import ru.pankov.repositories.PageRepository;
import ru.pankov.repositories.SiteRepository;

import java.time.LocalDateTime;

@Service
public class Transaction {
    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Transactional
    public void saveSite(){
        SiteEntity siteEntity = siteRepository.findByName("http://www.playback.ru/");
        if (siteEntity == null){
            siteEntity = new SiteEntity();
            siteEntity.setSiteStatus(SiteStatus.INDEXING);
            siteEntity.setName("test");
            siteEntity.setLastError("");
            siteEntity.setUrl("test");
            siteEntity.setStatusTime(LocalDateTime.now());
        }
        siteRepository.save(siteEntity);
        PageEntity pageEntity = new PageEntity();
        pageEntity.setCode(200);
        pageEntity.setPath("test");
        pageEntity.setContent("sadfgsadg");
        pageEntity.setSiteEntity(siteEntity);

        pageRepository.save(pageEntity);

        throw new OptimisticLockingFailureException("test");
    }
}
