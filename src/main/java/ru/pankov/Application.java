package ru.pankov;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        System.out.println("App run");
        SpringApplication app = new SpringApplication(Application.class);
        app.run(args);
        System.out.println("App finished");
    }

    @Bean
    public CommandLineRunner CommandLineRunnerBean() {
        return new CommandLineRunnerMain();
    }
}
