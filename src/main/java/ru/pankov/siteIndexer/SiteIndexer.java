package ru.pankov.siteIndexer;

import lombok.Data;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.pankov.dto.api.request.IndexRequest;
import ru.pankov.dto.api.response.IndexResponse;
import ru.pankov.dto.lemmanization.Lemma;
import ru.pankov.dto.siteparser.Page;
import ru.pankov.entities.*;
import ru.pankov.enums.SiteStatus;
import ru.pankov.pageParser.PageParser;
import ru.pankov.repositories.*;
import ru.pankov.lemmanization.Lemmatizer;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
@Service
@Scope("prototype")
public class SiteIndexer {
    private volatile Set<String> linksSet;
    private String mainPageUrl;
    private SiteIndexerTask initTask;

    @Autowired
    private ObjectProvider<SiteIndexerTask> taskObjectProvider;

    @Autowired
    @Qualifier("logger")
    private Logger logger;

    @Autowired
    private ForkJoinPool forkJoinPool;

    private SiteEntity siteEntity;

    private boolean interrupted;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private IndexRepository indexRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Value("${site-list}")
    private String[] siteList;


    @Autowired
    private PageParser pageParser;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private Lemmatizer lemmatizer;


    public SiteIndexer(String siteMainPageUrl) {
        linksSet = Collections.synchronizedSet(new HashSet<>());
        linksSet.add(mainPageUrl);
        mainPageUrl = siteMainPageUrl;
    }

    public void createIndex() {
        logger.info("Indexing start " + mainPageUrl);

        interrupted = false;
        siteEntity = addSiteDB(mainPageUrl);

        try {
            forkJoinPool.invoke(taskObjectProvider.getObject(mainPageUrl, this));

            if(interrupted){
                changeSiteStatus(siteEntity, SiteStatus.FAILED, "manual interrupt");
            }else {
                changeSiteStatus(siteEntity, SiteStatus.INDEXED, "");
            }
        } catch (StackOverflowError stackOverflowError) {
            logger.info(stackOverflowError.getMessage() + mainPageUrl);
            interrupted = true;
            changeSiteStatus(siteEntity, SiteStatus.FAILED, "critical error");
        }

        logger.info("Indexing finish " + mainPageUrl);
    }


    public IndexResponse indexSinglePage(IndexRequest indexRequest) {

        IndexResponse indexResponse = new IndexResponse();

        String site = "";
        String url = indexRequest.getUrl();
        String regexp = "^((http)|(https))://[a-z]*.[a-z]*.[a-z]{2}/";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            site = matcher.group(0);
        }

        linksSet = Collections.synchronizedSet(new HashSet<>());
        linksSet.add(mainPageUrl);
        mainPageUrl = site;

        if (Arrays.asList(siteList).contains(site)) {

            logger.info("Indexing page start " + url);
            siteEntity = addSiteDB(mainPageUrl);
            deletePage(url);
            indexPage(url, siteEntity);
            logger.info("Indexing page finish " + url);

            indexResponse.setResult(true);
        } else {
            indexResponse.setResult(false);
            indexResponse.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }

        return indexResponse;
    }

    private void changeSiteStatus(SiteEntity siteEntity, SiteStatus siteStatus, String error) {
        siteEntity.setSiteStatus(siteStatus);
        siteEntity.setLastError(error);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteEntity);
    }

    private SiteEntity addSiteDB(String mainPageUrl) {

        siteEntity = siteRepository.findByUrl(mainPageUrl);

        if (siteEntity == null) {
            siteEntity = new SiteEntity();
            siteEntity.setUrl(mainPageUrl);
            siteEntity.setName(mainPageUrl);
            siteEntity.setSiteStatus(SiteStatus.INDEXING);
            siteEntity.setStatusTime(LocalDateTime.now());

            return siteRepository.save(siteEntity);
        }

        return siteEntity;
    }

    public List<String> indexPage(String pageLink, SiteEntity siteEntity) {
        linksSet.add(pageLink);
        Page newPage = pageParser.parse(pageLink);
        newPage.setSiteEntity(siteEntity);
        newPage.setRelativePageLink(pageLink.replaceAll(siteEntity.getUrl(), ""));

        saveIndexPage(newPage);

        List<String> newPageLinks;
        List<String> pageLinks = newPage.getLinks().stream().map(l -> {
            String decodedLink = l;
            try {
                decodedLink = URLDecoder.decode(l,"utf-8");
            } catch (Exception e) {
                System.out.println(l);
                e.printStackTrace();
            }

            return decodedLink;
        }).collect(Collectors.toList());

        synchronized (this) {
            newPageLinks = pageLinks.stream().filter(link -> !linksSet.contains(link) && link.contains(siteEntity.getUrl())).distinct().toList();
            linksSet.addAll(newPageLinks);
        }

        return newPageLinks;
    }

    private void deletePage(String pageUrl) {

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

    private void saveIndexPage(Page newPage) {
        FieldEntity fieldTitle = fieldRepository.findByName("title");
        FieldEntity fieldBody = fieldRepository.findByName("body");

        PageEntity pageEntity = new PageEntity(newPage.getRelativePageLink(), newPage.getStatusCode(), newPage.getContent(), newPage.getSiteEntity());
        pageRepository.save(pageEntity);

        List<Lemma> lemmasTitle = lemmatizer.getLemmasWithRank(newPage.getTitleText(), fieldTitle.getWeight());
        List<Lemma> lemmasBody = lemmatizer.getLemmasWithRank(newPage.getContentText(), fieldBody.getWeight());

        lemmasTitle.addAll(lemmasBody);


        List<Lemma> lemmaTitleBody = lemmasTitle.stream()
                .collect(Collectors.toMap(e -> e, Lemma::getRank, Double::sum))
                .entrySet()
                .stream()
                .map(e -> {
                    Lemma lemma = e.getKey();
                    lemma.setRank(e.getValue());
                    return lemma;
                }).toList();

        List<LemmaEntity> lemmasNewAndOld;


        lemmasNewAndOld = saveLemmas(lemmaTitleBody, newPage);

        Map<String, Double> lemmaDoubleMap = lemmaTitleBody.stream().collect(Collectors.toMap(Lemma::getLemma, Lemma::getRank, Double::sum));

        List<IndexEntity> indexEntities = lemmasNewAndOld.stream().map(l -> new IndexEntity(pageEntity, l, lemmaDoubleMap.get(l.getLemma()))).toList();

        indexRepository.saveAll(indexEntities);
    }

    private List<LemmaEntity> saveLemmas(List<Lemma> lemmaList, Page newPage) {
        synchronized (newPage.getSiteEntity()) {
            List<LemmaEntity> lemmaEntities;
            List<LemmaEntity> lemmasNewAndOld;
            List<LemmaEntity> existedLemmas;

            existedLemmas = lemmaRepository.findByLemmaInAndSiteEntity(lemmaList.stream().map(Lemma::getLemma).toList(), newPage.getSiteEntity());

            lemmaEntities = lemmaList.stream().map(l -> new LemmaEntity(l.getLemma(), 1, newPage.getSiteEntity())).collect(Collectors.toList());
            lemmaEntities.addAll(existedLemmas);

            lemmasNewAndOld = lemmaEntities.stream().collect(Collectors.toMap(LemmaEntity::getLemma, e -> e, (val1, val2) -> {
                        if (val1.getId() != null) {
                            val1.setFrequency(val1.getFrequency() + val2.getFrequency());
                            return val1;
                        } else {
                            val2.setFrequency(val1.getFrequency() + val2.getFrequency());
                            return val2;
                        }
                    }))
                    .values()
                    .stream().toList();

            lemmaRepository.saveAll(lemmasNewAndOld);

            return lemmasNewAndOld;
        }
    }

}
