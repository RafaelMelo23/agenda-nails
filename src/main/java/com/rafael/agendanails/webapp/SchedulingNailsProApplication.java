package com.rafael.agendanails.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableCaching
@EnableMethodSecurity
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class SchedulingNailsProApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulingNailsProApplication.class, args);
    }
}