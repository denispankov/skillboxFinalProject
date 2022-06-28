package ru.pankov;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.pankov.siteparser.SiteIndexer;

import java.util.Arrays;

@SpringBootApplication
public class Application implements CommandLineRunner{
    public static void main(String[] args) {
        System.out.println("App run");
        SpringApplication app = new SpringApplication(Application.class);
        app.run(args);
        System.out.println("App finished");
    }


    @Override
    public void run(String... args) {
        SiteIndexer site = new SiteIndexer("https://www.svetlovka.ru/");
        site.createIndex();
        System.out.println("Index created");
            /*Lemmatizer lemmatizer = new Lemmatizer();

            String text = "Повторное появление леопарда в Осетии позволяет предположить, что\n" +
                    "леопард постоянно обитает в некоторых районах Северного Кавказа";

            Map<String, Long> lemmas = lemmatizer.getLemmas(text.toLowerCase());
            System.out.println(lemmas);*/
    }
}
