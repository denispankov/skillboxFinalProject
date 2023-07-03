package ru.pankov.services.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.pankov.dto.api.request.IndexRequest;
import ru.pankov.dto.api.response.IndexResponse;
import ru.pankov.entities.FieldEntity;
import ru.pankov.entities.SiteEntity;
import ru.pankov.enums.SiteStatus;
import ru.pankov.repositories.*;
import ru.pankov.services.IndexService;
import ru.pankov.siteIndexer.SiteIndexer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class IndexServiceImpl implements IndexService {
    
    @Autowired
    private IndexRepository indexRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private ObjectProvider<SiteIndexer> siteIndexerObjectProvider;

    @Autowired
    @Qualifier("logger")
    private Logger logger;

    @Value("${site-list}")
    private String[] siteList;
    private List<SiteIndexer> siteIndexerServices = new ArrayList<>();

    @PostConstruct
    private void postConstruct(){
        List<FieldEntity> fieldEntityList = new ArrayList<>();
        fieldEntityList.add(new FieldEntity("title","title",1));
        fieldEntityList.add(new FieldEntity("body","body",0.8f));

        fieldEntityList.forEach(f -> {
            FieldEntity searchField = fieldRepository.findByName(f.getName());
            if (searchField == null) {
                fieldRepository.save(f);
            }
        });
    }

    public IndexResponse indexAll(){
        String error = "";

        if (!indexingSiteExists()) {

            indexRepository.deleteAllWithQuery();
            pageRepository.deleteAllWithQuery();
            lemmaRepository.deleteAllWithQuery();
            siteRepository.deleteAllWithQuery();

            for (int i = 0; i < siteList.length; i++) {
                SiteIndexer siteIndexerService = siteIndexerObjectProvider.getObject(siteList[i]);
                Thread th = new Thread(siteIndexerService::createIndex);
                th.start();
                siteIndexerServices.add(siteIndexerService);
            }
        } else {
            error = "Индексация уже запущена";
        }

        return new IndexResponse(error.equals("") , error);
    }

    public IndexResponse stopAll(){
        IndexResponse indexResponse = new IndexResponse();
        String error = "";

        if (indexingSiteExists()) {

            siteIndexerServices.forEach(s -> s.setInterrupted(true));

            indexResponse.setResult(true);
        } else {
            error = "Индексация не запущена";
            indexResponse.setResult(false);
            indexResponse.setError(error);
        }

        return indexResponse;
    }

    public IndexResponse indexSinglePage(IndexRequest indexRequest){
        SiteIndexer indexer = siteIndexerObjectProvider.getObject();
        return indexer.indexSinglePage(indexRequest);
    }

    private boolean indexingSiteExists(){
        List<SiteEntity> siteEntities = siteRepository.findBySiteStatus(SiteStatus.INDEXING);

        return !siteEntities.isEmpty();
    }

    @PreDestroy
    private void onDestroy(){
        List<SiteEntity> siteEntities = siteRepository.findBySiteStatus(SiteStatus.INDEXING);
        siteEntities.forEach(e -> {
            e.setSiteStatus(SiteStatus.FAILED);
            e.setLastError("App closed");
            e.setStatusTime(LocalDateTime.now());
            siteRepository.save(e);
        });

    }
}
