package com.rafael.nailspro.webapp.application.retention;

import com.rafael.nailspro.webapp.domain.repository.RetentionForecastRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetentionForecastDeletionJob {

    private final RetentionForecastRepository repository;
    private final EntityManagerFactory entityManagerFactory;

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldForecasts() {
        int deleted = 0;
        Instant cutOffDate = Instant.now().minus(180, ChronoUnit.DAYS);
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            Session session = em.unwrap(Session.class);
            session.disableFilter("tenantFilter");

            deleted = repository.deleteByPredictedReturnDateBefore(cutOffDate);
        }

        if (deleted > 0) {
            log.info("Deleted {} retention forecasts older than {}", deleted, cutOffDate);
        }
    }
}