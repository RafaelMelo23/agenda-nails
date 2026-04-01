package com.rafael.agendanails.webapp.application.retention;

import com.rafael.agendanails.webapp.domain.enums.appointment.RetentionStatus;
import com.rafael.agendanails.webapp.domain.enums.evolution.EvolutionMessageStatus;
import com.rafael.agendanails.webapp.domain.model.Client;
import com.rafael.agendanails.webapp.domain.model.RetentionForecast;
import com.rafael.agendanails.webapp.domain.repository.*;
import com.rafael.agendanails.webapp.domain.whatsapp.SentMessageResult;
import com.rafael.agendanails.webapp.domain.whatsapp.WhatsappProvider;
import com.rafael.agendanails.webapp.support.BaseIntegrationTest;
import com.rafael.agendanails.webapp.support.factory.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FollowUpAutomationServiceIT extends BaseIntegrationTest {

    @Autowired
    private FollowUpAutomationService followUpAutomationService;
    @MockitoBean
    private WhatsappProvider whatsappProvider;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ProfessionalRepository professionalRepository;
    @Autowired
    private RetentionForecastRepository repository;
    @Autowired
    SalonServiceRepository salonServiceRepository;
    @Autowired
    private EntityManager entityManager;
    @MockitoSpyBean
    private VisitPredictionService visitPredictionService;
    @Autowired
    private AppointmentRepository appointmentRepository;

    private RetentionForecast saveForecastWithStatus(Instant predictedDate, RetentionStatus status) {
        var professional = professionalRepository.save(TestProfessionalFactory.standardForIt());
        var client = clientRepository.save(TestClientFactory.standardForIt());
        var service = salonServiceRepository.save(TestSalonServiceFactory.standardForIt());
        var appointment = appointmentRepository.save(TestAppointmentFactory.standardForIt(client, professional, service));

        return repository.save(TestRetentionForecastFactory.withAppointmentAndStatus(
                professional,
                client,
                new ArrayList<>(List.of(service)),
                appointment,
                predictedDate,
                status
        ));
    }

    private void saveForecastWithSpecificClient(Instant date, RetentionStatus status, Client client) {
        var prof = professionalRepository.save(TestProfessionalFactory.standardForIt());
        var service = salonServiceRepository.save(TestSalonServiceFactory.standardForIt());
        var appointment = appointmentRepository.save(TestAppointmentFactory.standardForIt(client, prof, service));

        repository.save(TestRetentionForecastFactory.withAppointmentAndStatus(
                prof, client, new ArrayList<>(List.of(service)), appointment, date, status
        ));
    }

    @Test
    void sendMaintenanceForecastMessage_shouldProcessPendingForecasts_WithinTwoDayWindow() {
        when(whatsappProvider.sendText(any(), any(), any()))
                .thenReturn(new SentMessageResult("msg-123", EvolutionMessageStatus.PENDING));

        Instant now = Instant.now();
        saveForecastWithStatus(now.plus(1, ChronoUnit.DAYS), RetentionStatus.PENDING);
        saveForecastWithStatus(now.plus(2, ChronoUnit.DAYS).minusSeconds(10), RetentionStatus.PENDING);
        saveForecastWithStatus(now.plus(3, ChronoUnit.DAYS), RetentionStatus.PENDING);
        entityManager.flush();
        entityManager.clear();

        followUpAutomationService.sendMaintenanceForecastMessage();

        verify(whatsappProvider, times(2)).sendText(any(), any(), any());
    }

    @Test
    void sendMaintenanceForecastMessage_doesNothing_whenForecastsAreNotPendingOrFailed() {
        Instant now = Instant.now();
        saveForecastWithStatus(now.plus(1, ChronoUnit.DAYS), RetentionStatus.CONVERTED);
        saveForecastWithStatus(now.plus(2, ChronoUnit.DAYS), RetentionStatus.EXPIRED);
        entityManager.flush();
        entityManager.clear();

        followUpAutomationService.sendMaintenanceForecastMessage();

        verify(whatsappProvider, times(0)).sendText(any(), any(), any());
    }

    @Test
    void sendMaintenanceForecastMessage_correctlyUpdatesStatuses_whenExceptionIsThrown() {
        Instant windowTime = Instant.now().plus(2, ChronoUnit.DAYS);

        for (int i = 0; i < 4; i++) {
            saveForecastWithStatus(windowTime, RetentionStatus.PENDING);
        }

        String targetPhone = "5511999999999";
        var failingClient = clientRepository.save(TestClientFactory.builder()
                .phoneNumber(targetPhone)
                .build());

        saveForecastWithSpecificClient(windowTime, RetentionStatus.PENDING, failingClient);

        entityManager.flush();
        entityManager.clear();

        when(whatsappProvider.sendText(any(), any(), any()))
                .thenReturn(new SentMessageResult("msg-123", EvolutionMessageStatus.PENDING));

        doThrow(new RuntimeException("WhatsApp API Failure"))
                .when(whatsappProvider)
                .sendText(any(), any(), eq(targetPhone));

        followUpAutomationService.sendMaintenanceForecastMessage();

        entityManager.flush();
        entityManager.clear();

        List<RetentionStatus> statuses = repository.findAll().stream()
                .map(RetentionForecast::getStatus)
                .toList();

        Map<RetentionStatus, Long> counts = statuses.stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        assertThat(counts)
                .hasSize(2)
                .containsEntry(RetentionStatus.NOTIFIED, 4L)
                .containsEntry(RetentionStatus.PENDING, 1L);
    }

    @Test
    void sendMaintenanceForecastMessage_shouldRetryForecasts_thatAreStillPending() {
        when(whatsappProvider.sendText(any(), any(), any()))
                .thenReturn(new SentMessageResult("msg-123", EvolutionMessageStatus.PENDING));

        Instant windowTime = Instant.now().plus(1, ChronoUnit.DAYS);
        for (int i = 0; i < 5; i++) {
            saveForecastWithStatus(windowTime, RetentionStatus.PENDING);
        }

        followUpAutomationService.sendMaintenanceForecastMessage();

        entityManager.flush();
        entityManager.clear();

        assertThat(repository.findAll())
                .hasSize(5)
                .extracting(RetentionForecast::getStatus)
                .containsOnly(RetentionStatus.NOTIFIED);
    }
}