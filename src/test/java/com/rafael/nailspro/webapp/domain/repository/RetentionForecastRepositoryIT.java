package com.rafael.nailspro.webapp.domain.repository;

import com.rafael.nailspro.webapp.domain.enums.appointment.RetentionStatus;
import com.rafael.nailspro.webapp.domain.model.*;
import com.rafael.nailspro.webapp.shared.tenant.TenantContext;
import com.rafael.nailspro.webapp.support.BaseIntegrationTest;
import com.rafael.nailspro.webapp.support.factory.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RetentionForecastRepositoryIT extends BaseIntegrationTest {

    private Professional professional;
    private Client client;
    private SalonService service;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        TenantContext.setTenant("tenant-test");
        professional = professionalRepository.save(TestProfessionalFactory.standardForIt("tenant-test"));
        client = clientRepository.save(TestClientFactory.standardForIt("tenant-test"));
        service = salonServiceRepository.save(TestSalonServiceFactory.standardForIt("tenant-test"));
        appointment = appointmentRepository.save(TestAppointmentFactory.standardForIt(client, professional, service));
    }

    @Test
    void shouldFindPredictedForecastsBetweenDates() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant start = now.plus(10, ChronoUnit.DAYS);
        Instant end = now.plus(20, ChronoUnit.DAYS);

        RetentionForecast rf = TestRetentionForecastFactory.complete(
                professional, client, List.of(service), appointment, now.plus(15, ChronoUnit.DAYS));
        retentionForecastRepository.save(rf);

        Appointment appointment2 = appointmentRepository.save(TestAppointmentFactory.standardForIt(client, professional, service));
        RetentionForecast outOfRange = TestRetentionForecastFactory.complete(
                professional, client, List.of(service), appointment2, now.plus(25, ChronoUnit.DAYS));
        retentionForecastRepository.save(outOfRange);

        List<RetentionForecast> results = retentionForecastRepository.findAllPredictedForecastsBetween(start, end, RetentionStatus.PENDING);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getPredictedReturnDate()).isBetween(start, end);
    }

    @Test
    void shouldFindExpiredPredictedForecasts() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        
        RetentionForecast expired = TestRetentionForecastFactory.complete(
                professional, client, List.of(service), appointment, now.minus(1, ChronoUnit.DAYS));
        retentionForecastRepository.save(expired);

        List<RetentionForecast> results = retentionForecastRepository.findAllExpiredPredictedForecastsByStatus(now, List.of(RetentionStatus.PENDING));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getPredictedReturnDate()).isBefore(now);
    }

    @Test
    void shouldFindWithJoins() {
        RetentionForecast rf = TestRetentionForecastFactory.complete(
                professional, client, List.of(service), appointment, Instant.now().plus(10, ChronoUnit.DAYS));
        RetentionForecast saved = retentionForecastRepository.save(rf);

        Optional<RetentionForecast> found = retentionForecastRepository.findWithJoins(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getClient()).isNotNull();
        assertThat(found.get().getOriginAppointment()).isNotNull();
        assertThat(found.get().getSalonServices()).isNotEmpty();
    }

    @Test
    @DisplayName("Should find forecasts regardless of current tenant context (Non-tenant aware)")
    void shouldFindForecastsRegardlessOfTenantContext() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        RetentionForecast rf = TestRetentionForecastFactory.complete(
                professional, client, List.of(service), appointment, now.plus(5, ChronoUnit.DAYS));
        retentionForecastRepository.save(rf);

        TenantContext.setTenant("another-tenant");
        List<RetentionForecast> results = retentionForecastRepository.findAllPredictedForecastsBetween(
                now, now.plus(10, ChronoUnit.DAYS), RetentionStatus.PENDING);

        assertThat(results).isNotEmpty();
    }
}