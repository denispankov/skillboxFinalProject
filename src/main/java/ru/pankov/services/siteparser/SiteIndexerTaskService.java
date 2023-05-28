package ru.pankov.services.siteparser;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.pankov.entities.IndexEntity;
import ru.pankov.entities.LemmaEntity;
import ru.pankov.entities.PageEntity;
import ru.pankov.entities.SiteEntity;
import ru.pankov.pojo.lemmanization.Lemma;
import ru.pankov.pojo.siteparser.Page;
import ru.pankov.repositories.IndexRepository;
import ru.pankov.repositories.LemmaRepository;
import ru.pankov.repositories.PageRepository;
import ru.pankov.services.lemmanization.Lemmatizer;

import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Scope("prototype")
public class SiteIndexerTaskService extends RecursiveAction {
    private String pageLink;
    private Set<String> linksSet;
    private String mainPageURL;

    private PageParserService pageParser;
    private ObjectProvider<SiteIndexerTaskService> taskObjectProvider;
    private SiteEntity siteEntity;

    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private IndexRepository indexRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private Lemmatizer lemmatizer;

    private Logger logger;

    @Autowired
    @Qualifier("logger")
    public void setLogger(Logger logger){
        this.logger = logger;
    }

    public SiteIndexerTaskService(String pageLink, Set<String> linksSet, String mainPageURL, SiteEntity siteEntity) {
        this.pageLink = pageLink;
        this.linksSet = linksSet;
        this.mainPageURL = mainPageURL;
        this.siteEntity = siteEntity;
    }

    @Autowired
    public void setTaskObjectProvider(ObjectProvider<SiteIndexerTaskService> taskObjectProvider) {
        this.taskObjectProvider = taskObjectProvider;
    }

    @Autowired
    public void setPageParser(PageParserService pageParser) {
        this.pageParser = pageParser;
    }


    @Override
    protected void compute() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        linksSet.add(pageLink);

        if (mainPageURL.equals(pageLink)){
            logger.info("Start parsing");
        }

        Page newPage = indexPage();

        List<String> pageLinks = newPage.getLinks();
        List<String> newPageLinks = pageLinks.stream().filter(link -> !linksSet.contains(link) & link.contains(mainPageURL)).distinct().collect(Collectors.toList());
        linksSet.addAll(newPageLinks);

        List<SiteIndexerTaskService> taskList = new ArrayList<>();
        for (String link : newPageLinks) {
            SiteIndexerTaskService task =  taskObjectProvider.getObject(link, linksSet, mainPageURL, siteEntity);
            task.fork();
            taskList.add(task);
        }

        for (SiteIndexerTaskService task : taskList) {
            task.join();
        }
        if (mainPageURL.equals(pageLink)){
            logger.info("End parsing");
        }

    }

    @Transactional
    public Page indexPage(){
        Page newPage = pageParser.parse(pageLink);
        newPage.setSiteEntity(siteEntity);
        newPage.setRelativePageLink(pageLink.replaceAll(mainPageURL, ""));

        PageEntity pageEntity = new PageEntity();
        pageEntity.setCode(newPage.getStatusCode());
        pageEntity.setPath(newPage.getRelativePageLink());
        pageEntity.setContent(newPage.getContent());
        pageEntity.setSiteEntity(newPage.getSiteEntity());

        pageRepository.save(pageEntity);

        List<Lemma> lemmasTitle = lemmatizer.getLemmas(newPage.getTitleText());
        List<Lemma> lemmasBody = lemmatizer.getLemmas(newPage.getContentText());
        lemmasTitle.forEach(l->l.setRank(l.getCount()));
        lemmasBody.forEach(l->l.setRank(l.getCount() * 0.8));
        Map<String, Double> lemmasMap = Stream.concat(lemmasBody.stream(), lemmasTitle.stream()).collect(Collectors.toMap(
                Lemma::getLemma,
                Lemma::getRank,
                (value1, value2) -> value1 + value2));
        List<Lemma> lemmas = lemmasMap.entrySet().stream()
                .map(l-> new Lemma(l.getKey(), l.getValue()))
                .sorted(Comparator.comparing(Lemma::getLemma)).collect(Collectors.toList());
        /*toDO
        получать существующие леммы и обновлять в них frequency
        подумать над многопточностью
        * */

        List<LemmaEntity> lemmaEntities = lemmas.stream().map(l-> new LemmaEntity(l.getLemma(), 1, newPage.getSiteEntity())).collect(Collectors.toList());

        for (LemmaEntity lemmaEntity: lemmaEntities){
            lemmaRepository.save(lemmaEntity);
        }

        List<IndexEntity> indexEntities = lemmaEntities.stream().map(l -> new IndexEntity(pageEntity, l, lemmasMap.get(l))).collect(Collectors.toList());

        for (IndexEntity indexEntity: indexEntities){
            indexRepository.save(indexEntity);
        }

        return newPage;
    }
}
