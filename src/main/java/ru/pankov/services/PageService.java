package ru.pankov.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pankov.entities.FieldEntity;
import ru.pankov.entities.IndexEntity;
import ru.pankov.entities.LemmaEntity;
import ru.pankov.entities.PageEntity;
import ru.pankov.dto.lemmanization.Lemma;
import ru.pankov.dto.siteparser.Page;
import ru.pankov.repositories.IndexRepository;
import ru.pankov.repositories.LemmaRepository;
import ru.pankov.repositories.PageRepository;
import ru.pankov.services.lemmanization.LemmatizerService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PageService {

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private LemmatizerService lemmatizer;

    @Autowired
    private IndexRepository indexRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private LemmaService lemmaService;

    @Autowired
    private FieldService fieldService;

    public void saveIndexPage(Page newPage) {
        FieldEntity fieldTitle = fieldService.getFieldByName("title");
        FieldEntity fieldBody = fieldService.getFieldByName("body");
        while (true) {
            try {
                PageEntity pageEntity = new PageEntity();
                pageEntity.setCode(newPage.getStatusCode());
                pageEntity.setPath(newPage.getRelativePageLink());
                pageEntity.setContent(newPage.getContent());
                pageEntity.setSiteEntity(newPage.getSiteEntity());

                pageRepository.save(pageEntity);

                List<Lemma> lemmasTitle = lemmatizer.getLemmas(newPage.getTitleText());
                List<Lemma> lemmasBody = lemmatizer.getLemmas(newPage.getContentText());
                lemmasTitle.forEach(l -> l.setRank(l.getCount() * fieldTitle.getWeight()));
                lemmasBody.forEach(l -> l.setRank(l.getCount() * fieldBody.getWeight()));

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

                while (true) {
                    try {
                        lemmasNewAndOld = lemmaService.saveLemmas(lemmaTitleBody, newPage);
                    } catch (DataIntegrityViolationException exception) {
                        continue;
                    }
                    break;
                }

                Map<String, Double> lemmaDoubleMap = lemmaTitleBody.stream().collect(Collectors.toMap(e -> e.getLemma(), e -> e.getRank(), Double::sum));

                List<IndexEntity> indexEntities = lemmasNewAndOld.stream().map(l -> new IndexEntity(pageEntity, l, lemmaDoubleMap.get(l.getLemma()))).collect(Collectors.toList());

                indexRepository.saveAll(indexEntities);
            } catch (DataIntegrityViolationException exception) {
                continue;
            }
            break;
        }
    }

    @Transactional
    public void deletePage(String pageUrl) {

        PageEntity pageEntity = pageRepository.findByPath(pageUrl);

        List<IndexEntity> indexEntityList = indexRepository.findByPageEntity(pageEntity);
        indexRepository.deleteAll(indexEntityList);

        for (IndexEntity indexEntity : indexEntityList) {
            LemmaEntity lemmaEntity = indexEntity.getLemmaEntity();
            lemmaEntity.setFrequency(lemmaEntity.getFrequency() - 1);
            lemmaRepository.save(lemmaEntity);
        }

        pageRepository.delete(pageEntity);

    }

    public void deleteAll(){
        pageRepository.deleteAllWithQuery();
    }
}
