package ru.pankov.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.pankov.dto.statistic.ResultStatistic;
import ru.pankov.dto.statistic.SitesDetailStatistic;
import ru.pankov.dto.statistic.TotalStatistic;
import ru.pankov.enums.SiteStatus;
import ru.pankov.repositories.LemmaRepository;
import ru.pankov.repositories.PageRepository;
import ru.pankov.repositories.SiteRepository;
import ru.pankov.services.StatisticService;

import java.util.List;

@Service
public class StatisticServiceImpl implements StatisticService {

    @Autowired
    SiteRepository siteRepository;

    @Autowired
    PageRepository pageRepository;

    @Autowired
    LemmaRepository lemmaRepository;


    public ResultStatistic getStatistic() {

        TotalStatistic statisticTotal = new TotalStatistic(siteRepository.count()
                ,pageRepository.count()
                ,lemmaRepository.count()
                ,siteRepository.countBySiteStatus(SiteStatus.INDEXING) > 0);


        List<SitesDetailStatistic> sitesDetailStatistics = siteRepository.findAll()
                .stream()
                .map(s -> new SitesDetailStatistic(s.getUrl(),
                        s.getName(),
                        s.getSiteStatus().toString(),
                        s.getStatusTime().toString(),
                        s.getLastError(),
                        pageRepository.countBySiteEntity(s),
                        lemmaRepository.countBySiteEntity(s)))
                .toList();

        return new ResultStatistic(statisticTotal, sitesDetailStatistics, true);
    }
}
