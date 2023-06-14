package ru.pankov.dto.statistic;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResultStatistic{
    @Data
    class Statistic{
        public Statistic(){
            total = null;
            detailed = new ArrayList<>();
        }

        private TotalStatistic total;
        private List<SitesDetailStatistic> detailed;

        public Statistic(TotalStatistic total, List<SitesDetailStatistic> detailed) {
            this.total = total;
            this.detailed = detailed;
        }
    }
    private boolean result;
    private Statistic statistics;

    public ResultStatistic(TotalStatistic totalStatistic,List<SitesDetailStatistic> detailed){
        statistics = new Statistic(totalStatistic, detailed);
    }
}
