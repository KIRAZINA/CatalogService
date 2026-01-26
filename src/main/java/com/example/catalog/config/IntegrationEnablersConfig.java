package com.example.catalog.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@Profile("!local")
@EnableKafka
@EnableCaching
@EnableElasticsearchRepositories(basePackages = "com.example.catalog.repository.elasticsearch")
public class IntegrationEnablersConfig {
}
