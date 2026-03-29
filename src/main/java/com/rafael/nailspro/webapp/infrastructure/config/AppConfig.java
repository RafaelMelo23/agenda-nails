package com.rafael.nailspro.webapp.infrastructure.config;


import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {

        return builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Bean
    public RestTemplate evolutionRestTemplate(RestTemplateBuilder builder) {

        return builder
                .connectTimeout(Duration.ofSeconds(15))
                .readTimeout(Duration.ofSeconds(45))
                .build();
    }
}