package ru.pankov.restcontrollers;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResultStatistic{
    @Data
    class Total{
        private long sites;
        private long pages;
        private long lemmas;
        private boolean isIndexing;

        public Total(long sites, long pages, long lemmas, boolean isIndexing) {
            this.sites = sites;
            this.pages = pages;
            this.lemmas = lemmas;
            this.isIndexing = isIndexing;
        }
    }
    @Data
    class SiteDet{
        private String url;
        private String name;
        private String status;
        private String statusTime;
        private String error;
        private int pages;
        private long lemmas;

        public SiteDet(String url, String name, String status, String statusTime, String error, int pages, long lemmas) {
            this.url = url;
            this.name = name;
            this.status = status;
            this.statusTime = statusTime;
            this.error = error;
            this.pages = pages;
            this.lemmas = lemmas;
        }
    }
    @Data
    class Statistic{
        public Statistic(){
            total = null;
            detailed = new ArrayList<>();
        }
        public void addDetail(SiteDet det){
            detailed.add(det);
        }

        private Total total;
        private List<SiteDet> detailed;
    }
    private boolean result;
    private Statistic statistics;

    public ResultStatistic(){
        statistics = new Statistic();
    }

    public void setTotal(long sites, long pages, long lemmas, boolean isIndexing){
        statistics.setTotal(new Total(sites, pages, lemmas, isIndexing));
    }

    public void addDetail(String url, String name, String status, String statusTime, String error, int pages, long lemmas){
        statistics.addDetail(new SiteDet(url, name, status, statusTime, error, pages, lemmas));
    }
}
