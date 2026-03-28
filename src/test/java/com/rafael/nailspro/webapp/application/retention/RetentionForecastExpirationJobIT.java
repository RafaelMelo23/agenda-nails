package com.rafael.nailspro.webapp.application.retention;

import com.rafael.nailspro.webapp.domain.enums.appointment.RetentionStatus;
import com.rafael.nailspro.webapp.domain.model.RetentionForecast;
import com.rafael.nailspro.webapp.domain.repository.ClientRepository;
import com.rafael.nailspro.webapp.domain.repository.ProfessionalRepository;
import com.rafael.nailspro.webapp.domain.repository.RetentionForecastRepository;
import com.rafael.nailspro.webapp.domain.repository.SalonServiceRepository;
import com.rafael.nailspro.webapp.support.BaseIntegrationTest;
import com.rafael.nailspro.webapp.support.factory.TestClientFactory;
import com.rafael.nailspro.webapp.support.factory.TestProfessionalFactory;
import com.rafael.nailspro.webapp.support.factory.TestRetentionForecastFactory;
import com.rafael.nailspro.webapp.support.factory.TestSalonServiceFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("it")
class RetentionForecastExpirationJobIT extends BaseIntegrationTest {

    @Autowired
    private RetentionForecastExpirationJob retentionForecastExpirationJob;
    @Autowired
    private ProfessionalRepository professionalRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private RetentionForecastRepository repository;
    @Autowired
    SalonServiceRepository salonServiceRepository;
    @Autowired
    private EntityManager entityManager;
    @MockitoSpyBean
    private VisitPredictionService visitPredictionService;

    private List<RetentionForecast> persistExpiredForecasts(int count) {
        List<RetentionForecast> forecasts = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            var professional = professionalRepository.save(TestProfessionalFactory.standardForIt());
            var client = clientRepository.save(TestClientFactory.standardForIt());
            var service = salonServiceRepository.save(TestSalonServiceFactory.standardForIt());

            var forecast = TestRetentionForecastFactory.expiredForIt(professional, client, List.of(service));
            forecasts.add(forecast);
        }
        var savedForecasts = repository.saveAll(forecasts);
        entityManager.flush();
        return savedForecasts;
    }

    private List<RetentionForecast> persistActiveForecasts(int count) {
        List<RetentionForecast> forecasts = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            var professional = professionalRepository.save(TestProfessionalFactory.standardForIt());
            var client = clientRepository.save(TestClientFactory.standardForIt());
            var service = salonServiceRepository.save(TestSalonServiceFactory.standardForIt());

            var forecast = TestRetentionForecastFactory.activeWithOldReturnDate(professional, client, List.of(service));
            forecasts.add(forecast);
        }
        var savedForecasts = repository.saveAll(forecasts);
        entityManager.flush();
        return savedForecasts;
    }

    @Test
    void expireForecasts_successfullyExpiresAll() {
        persistActiveForecasts(10);

        retentionForecastExpirationJob.expireForecasts();

        List<RetentionForecast> results = repository.findAll();

        assertThat(results)
                .hasSize(10)
                .extracting(RetentionForecast::getStatus)
                .containsOnly(RetentionStatus.EXPIRED);
    }

    @Test
    void expireForecasts_successfullyExpiresActiveForecasts_WhenHalfAreAlreadyExpired() {
        persistExpiredForecasts(5);
        List<RetentionForecast> activeForecasts = persistActiveForecasts(5);

        retentionForecastExpirationJob.expireForecasts();

        List<RetentionForecast> results = repository.findAll();

        assertThat(results)
                .hasSize(10)
                .extracting(RetentionForecast::getStatus)
                .containsOnly(RetentionStatus.EXPIRED);

        assertThat(activeForecasts)
                .hasSize(5)
                .extracting(RetentionForecast::getStatus)
                .containsOnly(RetentionStatus.EXPIRED);
    }

    @Test
    void expireForecasts_successfullyExpiresActiveForecasts_WithVariousTenants() {
        persistActiveForecasts(1); // <- its tenant id is tenant-test

        String newTenant = "new-tenant";
        var professional = professionalRepository.save(TestProfessionalFactory.standardForIt(newTenant));
        var client = clientRepository.save(TestClientFactory.standardForIt(newTenant));
        var service = salonServiceRepository.save(TestSalonServiceFactory.standardForIt(newTenant));
        repository.save(TestRetentionForecastFactory.activeWithOldReturnDate(professional, client, List.of(service)));
        entityManager.flush();

        retentionForecastExpirationJob.expireForecasts();

        assertThat(repository.findAll())
                .hasSize(2)
                .extracting(RetentionForecast::getStatus)
                .containsOnly(RetentionStatus.EXPIRED);
    }

    @Test
    void expireForecasts_successfullyExpiresActive_evenWhenOneThrowsException() {
        var forecasts = persistActiveForecasts(10);
        var failedForecast = forecasts.get(1);

        doThrow(new RuntimeException("DB Connection Timeout"))
                .when(visitPredictionService)
                .markForecastAsExpired(failedForecast);

        retentionForecastExpirationJob.expireForecasts();

        List<RetentionForecast> allResults = repository.findAll();

        assertThat(allResults).hasSize(10);

        long expiredCount = allResults.stream()
                .filter(r -> r.getStatus() == RetentionStatus.EXPIRED)
                .count();
        assertThat(expiredCount).isEqualTo(9);

        var updatedFailedForecast = repository.findById(failedForecast.getId()).orElseThrow();
        assertThat(updatedFailedForecast.getStatus()).isNotEqualTo(RetentionStatus.EXPIRED);
    }

    @Test
    void expireForecasts_shouldNotExpireConvertedForecasts() {
        var professional = professionalRepository.save(TestProfessionalFactory.standardForIt());
        var client = clientRepository.save(TestClientFactory.standardForIt());
        var service = salonServiceRepository.save(TestSalonServiceFactory.standardForIt());

        var forecast = TestRetentionForecastFactory.activeWithOldReturnDate(professional, client, List.of(service));
        forecast.setStatus(RetentionStatus.CONVERTED);
        repository.save(forecast);
        entityManager.flush();

        retentionForecastExpirationJob.expireForecasts();

        var updatedForecast = repository.findById(forecast.getId()).orElseThrow();
        assertThat(updatedForecast.getStatus()).isEqualTo(RetentionStatus.CONVERTED);
    }

    @Test
    void expireForecasts_shouldExpireNotifiedForecasts() {
        var professional = professionalRepository.save(TestProfessionalFactory.standardForIt());
        var client = clientRepository.save(TestClientFactory.standardForIt());
        var service = salonServiceRepository.save(TestSalonServiceFactory.standardForIt());

        var forecast = TestRetentionForecastFactory.activeWithOldReturnDate(professional, client, List.of(service));
        forecast.setStatus(RetentionStatus.NOTIFIED);
        repository.save(forecast);
        entityManager.flush();

        retentionForecastExpirationJob.expireForecasts();

        var updatedForecast = repository.findById(forecast.getId()).orElseThrow();
        assertThat(updatedForecast.getStatus()).isEqualTo(RetentionStatus.EXPIRED);
    }
}