package ru.pankov.controllers.statistic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pankov.dto.statistic.ResultStatistic;
import ru.pankov.services.StatisticService;
import ru.pankov.services.impl.StatisticServiceImpl;

@RestController
public class StatisticController {

    @Autowired
    private StatisticService statisticService;

    @GetMapping("/api/statistics")
    public ResultStatistic getStatistic() {

        return statisticService.getStatistic();
    }
}
