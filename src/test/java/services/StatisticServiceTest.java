package services;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.pankov.BeanConfiguration;
import ru.pankov.dto.statistic.ResultStatistic;
import ru.pankov.dto.statistic.SitesDetailStatistic;
import ru.pankov.dto.statistic.TotalStatistic;
import ru.pankov.entities.LemmaEntity;
import ru.pankov.entities.PageEntity;
import ru.pankov.entities.SiteEntity;
import ru.pankov.enums.SiteStatus;
import ru.pankov.repositories.LemmaRepository;
import ru.pankov.repositories.PageRepository;
import ru.pankov.repositories.SiteRepository;
import ru.pankov.services.StatisticService;
import ru.pankov.services.impl.StatisticServiceImpl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ContextConfiguration(classes = {BeanConfiguration.class})
@DataJpaTest
public class StatisticServiceTest {
    @Autowired
    private StatisticService statisticService;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    private ResultStatistic resultStatistic;

    private TotalStatistic totalStatistic;

    private  List<SitesDetailStatistic> sitesDetailStatistics;

    @BeforeEach
    public void init(){
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName("testSite");
        siteEntity.setSiteStatus(SiteStatus.INDEXING);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setUrl("https://test.ru");

        siteRepository.save(siteEntity);

        LemmaEntity lemmaEntity = new LemmaEntity();
        lemmaEntity.setSiteEntity(siteEntity);
        lemmaEntity.setLemma("тест");
        lemmaEntity.setFrequency(1);

        lemmaRepository.save(lemmaEntity);

        PageEntity pageEntity = new PageEntity();
        pageEntity.setCode(200);
        pageEntity.setSiteEntity(siteEntity);
        pageEntity.setPath("/test_patch");
        pageEntity.setContent("test content");

        Set<LemmaEntity> lemmaEntitySet = new HashSet<>();
        lemmaEntitySet.add(lemmaEntity);
        pageEntity.setLemmaEntitySet(lemmaEntitySet);

        pageRepository.save(pageEntity);

        resultStatistic = statisticService.getStatistic();

        totalStatistic = resultStatistic.getTotalStatistic();
        sitesDetailStatistics = resultStatistic.getDetailedStatistic();

    }
    @Test
    @DisplayName("Page quantity")
    public void testPagesStatistic(){
        long pageQuantity = totalStatistic.getPages();

        Assertions.assertEquals(1, pageQuantity);
    }

    @Test
    @DisplayName("Lemmas quantity")
    public void testLemmasStatistic(){
        long lemmasQuantity = totalStatistic.getLemmas();

        Assertions.assertEquals(1, lemmasQuantity);
    }

    @Test
    @DisplayName("Site quantity")
    public void testSitesStatistic(){
        long sitesQuantity = totalStatistic.getSites();

        Assertions.assertEquals(1, sitesQuantity);
    }

    @Test
    @DisplayName("Indexing status")
    public void testIndexing(){
        boolean isIndexing = totalStatistic.isIndexing();

        Assertions.assertEquals(true, isIndexing);
    }

    public void testQuantityOfDetailed(){
        Assertions.assertEquals(1,sitesDetailStatistics.size());
    }

    public void testQuantityPagesBySite(){
        int pageQuantity = sitesDetailStatistics.get(0).getPages();

        Assertions.assertEquals(1, pageQuantity);
    }

    public void testQuantityLemmas(){
        long lemmasQuantity = sitesDetailStatistics.get(0).getLemmas();

        Assertions.assertEquals(1, lemmasQuantity);
    }

    @AfterEach
    public void deleteData(){
        siteRepository.deleteAllWithQuery();
        pageRepository.deleteAllWithQuery();
        lemmaRepository.deleteAllWithQuery();
    }
}
