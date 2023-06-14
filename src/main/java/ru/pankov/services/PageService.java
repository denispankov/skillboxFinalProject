package ru.pankov.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pankov.entities.IndexEntity;
import ru.pankov.entities.LemmaEntity;
import ru.pankov.entities.PageEntity;
import ru.pankov.dto.lemmanization.Lemma;
import ru.pankov.dto.siteparser.Page;
import ru.pankov.repositories.IndexRepository;
import ru.pankov.repositories.LemmaRepository;
import ru.pankov.repositories.PageRepository;
import ru.pankov.services.lemmanization.Lemmatizer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PageService {

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private Lemmatizer lemmatizer;

    @Autowired
    private IndexRepository indexRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private LemmaService lemmaService;

    public void indexPage(Page newPage) {
        PageEntity pageEntity = new PageEntity();
        pageEntity.setCode(newPage.getStatusCode());
        pageEntity.setPath(newPage.getRelativePageLink());
        pageEntity.setContent(newPage.getContent());
        pageEntity.setSiteEntity(newPage.getSiteEntity());

        pageRepository.save(pageEntity);

        List<Lemma> lemmasTitle = lemmatizer.getLemmas(newPage.getTitleText());
        List<Lemma> lemmasBody = lemmatizer.getLemmas(newPage.getContentText());
        lemmasTitle.forEach(l -> l.setRank(l.getCount()));
        lemmasBody.forEach(l -> l.setRank(l.getCount() * 0.8));

        lemmasTitle.addAll(lemmasBody);


        List<Lemma> lemmaTitleBody = lemmasTitle.stream()
                .collect(Collectors.toMap(e -> e, e -> e.getRank(), Double::sum))
                .entrySet()
                .stream()
                .map(e -> {
                    Lemma lemma = e.getKey();
                    lemma.setRank(e.getValue());
                    return lemma;
                }).collect(Collectors.toList());

        List<LemmaEntity> lemmasNewAndOld;

        lemmasNewAndOld = lemmaService.saveLemmas(lemmaTitleBody, newPage);

        Map<String, Double> lemmaDoubleMap = lemmaTitleBody.stream().collect(Collectors.toMap(e -> e.getLemma(), e -> e.getRank(), Double::sum));

        List<IndexEntity> indexEntities = lemmasNewAndOld.stream().map(l -> new IndexEntity(pageEntity, l, lemmaDoubleMap.get(l.getLemma()))).collect(Collectors.toList());

        indexRepository.saveAll(indexEntities);
    }

    @Transactional
    public void deletePage(String pageUrl) {

        PageEntity pageEntity = pageRepository.findByPath(pageUrl);

        List<IndexEntity> indexEntityList = indexRepository.findByPageEntity(pageEntity);
        indexRepository.deleteAll(indexEntityList);

        for(IndexEntity indexEntity: indexEntityList){
            LemmaEntity lemmaEntity = indexEntity.getLemmaEntity();
            lemmaEntity.setFrequency(lemmaEntity.getFrequency() - 1);
            lemmaRepository.save(lemmaEntity);
        }

        pageRepository.delete(pageEntity);

    }
}
