package ru.pankov.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pankov.dto.statistic.ResultStatistic;
import ru.pankov.services.StatisticService;

@RestController
public class StatisticController {

    @Autowired
    private StatisticService statisticService;

    @GetMapping("/api/statistics")
    public ResultStatistic getStatistic() {

        ResultStatistic result = statisticService.getStatistic();;
        result.setResult(true);
        return result;
    }
}
