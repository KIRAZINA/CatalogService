package com.example.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CatalogServiceApplication.class);
        app.setDefaultProperties(Map.of("spring.profiles.default", "local"));
        app.run(args);
    }
}
