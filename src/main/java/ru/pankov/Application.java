package ru.pankov;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.pankov.siteparser.SiteIndexer;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx, JdbcTemplate jdbcTemplate){
        return args -> {

            SiteIndexer site = new SiteIndexer("https://dombulgakova.ru/");

            site.createIndex();
        };
    }
}
