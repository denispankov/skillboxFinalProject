package ru.pankov;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.ForkJoinPool;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"ru.pankov"})
@EnableJpaRepositories(basePackages = {"ru.pankov.repositories"})
public class BeanConfiguration {

    @Bean(name="logger")
    public Logger getLogger(){
        return LoggerFactory.getLogger("main");
    }

    @Bean
    public ForkJoinPool getForkJoinPool(){
        return new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }

}
