package ru.pankov;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {
    @Bean(name="logger")
    public Logger getLogger(){
        return LoggerFactory.getLogger("main");
    }
}
