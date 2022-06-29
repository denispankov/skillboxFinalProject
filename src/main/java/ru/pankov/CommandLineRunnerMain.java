package ru.pankov;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import ru.pankov.siteparser.SiteIndexer;

import java.util.Arrays;


public class CommandLineRunnerMain implements CommandLineRunner {
    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private ObjectProvider<SiteIndexer> siteIndexerObjectProvider;

    @Override
    public void run(String... args) throws Exception {

        SiteIndexer site = siteIndexerObjectProvider.getObject("https://www.svetlovka.ru/");
        //Arrays.stream(ctx.getBeanDefinitionNames()).forEach(System.out::println);
        site.createIndex();
        System.out.println("Index created");
        /* TODO
        Перед переходом на веб
        добавить логирование
        добавить юнит тесты
         */
    }
}
