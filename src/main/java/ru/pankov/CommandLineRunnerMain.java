package ru.pankov;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import ru.pankov.siteparser.SiteIndexer;

import java.util.Arrays;
import java.util.List;


public class CommandLineRunnerMain implements CommandLineRunner {
    @Autowired
    private ObjectProvider<SiteIndexer> siteIndexerObjectProvider;
    @Value("${site-list}")
    private String[] siteList;
    private class SiteIndexThread extends Thread {
        private String siteUrl;
        public SiteIndexThread(String siteUrl){
            super();
            this.siteUrl = siteUrl;
        }
        public void run(){
            SiteIndexer site = siteIndexerObjectProvider.getObject(siteUrl);
            site.createIndex();
        }
    }


    @Override
    public void run(String... args) throws Exception {

        for(int i = 0; i<siteList.length; i++){
            new SiteIndexThread(siteList[i]).start();
        }
        System.out.println("Index created");
        /* TODO
        Перед переходом на веб
        добавить логирование
        добавить юнит тесты
         */
    }
}
