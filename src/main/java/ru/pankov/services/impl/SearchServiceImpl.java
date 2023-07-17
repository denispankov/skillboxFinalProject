package ru.pankov.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.pankov.dto.api.response.SearchResponse;
import ru.pankov.dto.lemmanization.Lemma;
import ru.pankov.dto.search.Page;
import ru.pankov.dto.search.SearchResult;
import ru.pankov.entities.IndexEntity;
import ru.pankov.entities.LemmaEntity;
import ru.pankov.entities.PageEntity;
import ru.pankov.entities.SiteEntity;
import ru.pankov.repositories.IndexRepository;
import ru.pankov.repositories.LemmaRepository;
import ru.pankov.repositories.PageRepository;
import ru.pankov.repositories.SiteRepository;
import ru.pankov.lemmanization.Lemmatizer;
import ru.pankov.pageParser.PageParser;
import ru.pankov.services.SearchService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    Lemmatizer lemmatizer;
    @Autowired
    SiteRepository siteRepository;

    @Autowired
    private PageParser pageParser;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private IndexRepository indexRepository;

    private final int OVER_FREQUENCY_PERCENT = 60;


    public SearchResponse search(Map<String, String> req) {
        String query = req.get("query");
        String site = req.get("site") == null ? "" : req.get("site");
        String offsetS = req.get("offset");
        String limitS = req.get("limit");

        int offset = offsetS == null ? 0 : Integer.parseInt(offsetS);
        int limit = limitS == null ? 0 : Integer.parseInt(limitS);

        SearchResponse result = new SearchResponse();

        SiteEntity siteEntity = siteRepository.findByUrl(site);

        if(query != null && !query.equals("")) {

            List<Lemma> requestLemmas = lemmatizer.getLemmas(query);

            requestLemmas.forEach(l -> l.setFrequency(lemmaRepository.findByLemma(l.getLemma())
                    .stream()
                    .mapToInt(LemmaEntity::getFrequency).sum()));

            requestLemmas = excludeLemmas(requestLemmas).stream().sorted(Comparator.comparingInt(Lemma::getFrequency)).collect(Collectors.toList());

            List<PageEntity> pageEntities = findPages(requestLemmas, siteEntity);
            List<SearchResult> results = new ArrayList<>();

            if (!pageEntities.isEmpty()) {

                List<Page> pageList = calculateRelevance(pageEntities, requestLemmas);

                result.setCount(pageList.size());

                int toIndex = Math.min(offset + limit, pageList.size());
                pageList = pageList.subList(offset, toIndex);

                for (Page page : pageList) {
                    SearchResult searchResultTemp = new SearchResult(page.getPageEntity().getPath(),
                            pageParser.getHTMLTitle(page.getPageEntity().getContent()),
                            pageParser.getHTMLSnippet(page.getPageEntity().getContent(), requestLemmas),
                            page.getRelativeRelevance(),
                            pageList.size(),
                            page.getPageEntity().getSiteEntity().getName(),
                            page.getPageEntity().getSiteEntity().getUrl());
                    results.add(searchResultTemp);
                }
            }


            for (SearchResult res : results) {
                result.fromSearchResult(res);
            }

            result.setError("");
            result.setResult(true);

        }else {
            result.setError("Задан пустой поисковый запрос");
            result.setResult(false);
        }
        return result;
    }

    private List<Lemma> excludeLemmas(List<Lemma> lemmasList) {
        long pageQuantity = pageRepository.count();
        List<Lemma> noExcludeLemmas = new ArrayList<>();

        for (Lemma lemma : lemmasList) {
            double frequencyPercent = (double) lemma.getFrequency() / (double) pageQuantity * 100d;
            if (frequencyPercent < OVER_FREQUENCY_PERCENT && frequencyPercent != 0) {
                noExcludeLemmas.add(lemma);
            }
        }

        if (noExcludeLemmas.isEmpty()){
            noExcludeLemmas.add(lemmasList.get(lemmasList.size()-1));
        }

        return noExcludeLemmas;
    }

    private List<PageEntity> findPages(List<Lemma> lemmaEntities, SiteEntity siteEntity) {
        List<PageEntity> pageEntities = new ArrayList<>();
        for (Lemma lemma : lemmaEntities) {
            if (pageEntities.isEmpty()) {
                List<LemmaEntity> lemmaEntityList;
                if (siteEntity == null){
                    lemmaEntityList = lemmaRepository.findByLemma(lemma.getLemma());
                }else{
                    lemmaEntityList = lemmaRepository.findByLemmaAndSiteEntity(lemma.getLemma(), siteEntity);
                }
                pageEntities = lemmaEntityList
                        .stream().flatMap(l -> l.getPageEntitySet()
                                .stream().distinct().collect(Collectors.toList()).stream()).collect(Collectors.toList());
            } else {

                pageEntities = indexRepository.findByPageEntityInAndLemmaEntityIn(pageEntities, lemmaRepository.findByLemma(lemma.getLemma()))
                        .stream()
                        .map(IndexEntity::getPageEntity).distinct().collect(Collectors.toList());
            }
        }

        return pageEntities;
    }

    private List<Page> calculateRelevance(List<PageEntity> pageEntities, List<Lemma> lemmaList) {
        List<String> lemmaNamesList = lemmaList
                .stream()
                .map(Lemma::getLemma).collect(Collectors.toList());
        List<Page> pageList = pageEntities
                .stream()
                .map(p -> new Page(p, indexRepository.sumRankByPageIdAndLemmaNameIn(p.getId(), lemmaNamesList))).collect(Collectors.toList());

        double maxAbsoluteRelevance = Collections.max(pageList).getAbsoluteRelevance();

        pageList.forEach(p -> p.setRelativeRelevance(p.getAbsoluteRelevance()/ maxAbsoluteRelevance));

        return pageList.stream().sorted().collect(Collectors.toList());
    }
}
