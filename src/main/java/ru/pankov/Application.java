package ru.pankov;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.pankov.lemmanization.Lemmatizer;
import ru.pankov.siteparser.SiteIndexer;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx, JdbcTemplate jdbcTemplate){
        return args -> {

            SiteIndexer site = new SiteIndexer("https://dombulgakova.ru/");

            //site.createIndex();
            Lemmatizer lemmatizer = new Lemmatizer();

            String text = "Повторное появление леопарда в Осетии позволяет предположить, что\n" +
                    "леопард постоянно обитает в некоторых районах Северного Кавказа";

            Map<String, Long> lemmas = lemmatizer.getLemmas(text.toLowerCase());
            System.out.println(lemmas);
        };
    }
}
