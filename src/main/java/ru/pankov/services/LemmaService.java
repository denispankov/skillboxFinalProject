package ru.pankov.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.pankov.entities.LemmaEntity;
import ru.pankov.dto.lemmanization.Lemma;
import ru.pankov.dto.siteparser.Page;
import ru.pankov.repositories.LemmaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LemmaService {

    @Autowired
    private LemmaRepository lemmaRepository;


    synchronized public List<LemmaEntity> saveLemmas(List<Lemma> lemmaList, Page newPage) {
        List<LemmaEntity> lemmaEntities;
        List<LemmaEntity> lemmasNewAndOld;
        List<LemmaEntity> existedLemmas;

        existedLemmas = lemmaRepository.findByLemmaIn(lemmaList.stream().map(l -> l.getLemma()).collect(Collectors.toList()));

        lemmaEntities = lemmaList.stream().map(l -> new LemmaEntity(l.getLemma(), 1, newPage.getSiteEntity())).collect(Collectors.toList());
        lemmaEntities.addAll(existedLemmas);

        lemmasNewAndOld = lemmaEntities.stream().collect(Collectors.toMap(e -> e.getLemma(), e -> e, (val1, val2) -> {
                    if (val1.getId() != null) {
                        val1.setFrequency(val1.getFrequency() + val2.getFrequency());
                        return val1;
                    } else {
                        val2.setFrequency(val1.getFrequency() + val2.getFrequency());
                        return val2;
                    }
                }))
                .entrySet()
                .stream()
                .map(e -> e.getValue()).collect(Collectors.toList());

        lemmaRepository.saveAll(lemmasNewAndOld);

        return lemmasNewAndOld;
    }
}