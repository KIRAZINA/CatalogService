package com.example.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Map;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.catalog.repository")
@EnableTransactionManagement
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CatalogServiceApplication.class);
        app.setDefaultProperties(Map.of("spring.profiles.default", "local"));
        app.run(args);
    }
}