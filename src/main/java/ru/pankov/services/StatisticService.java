package ru.pankov.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.pankov.dto.statistic.ResultStatistic;
import ru.pankov.dto.statistic.SitesDetailStatistic;
import ru.pankov.dto.statistic.TotalStatistic;
import ru.pankov.dto.interfaces.TotalStatisticInterface;
import ru.pankov.repositories.SiteRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticService {

    @Autowired
    SiteRepository siteRepository;

    public ResultStatistic getStatistic() {

        TotalStatisticInterface totalStatistic = siteRepository.getStatisticTotal();
        TotalStatistic statisticTotal = new TotalStatistic(totalStatistic.getSites(), totalStatistic.getPages(), totalStatistic.getLemmas(), totalStatistic.getIsIndexing());

        List<SitesDetailStatistic> sitesDetailStatistics = siteRepository.getStatisticDetail()
                .stream()
                .map(s -> new SitesDetailStatistic(s.getUrl(),
                        s.getName(),
                        s.getStatus(),
                        s.getStatusTime(),
                        s.getError(),
                        s.getPages(),
                        s.getLemmas()))
                .collect(Collectors.toList());

        return new ResultStatistic(statisticTotal, sitesDetailStatistics);
    }
}
