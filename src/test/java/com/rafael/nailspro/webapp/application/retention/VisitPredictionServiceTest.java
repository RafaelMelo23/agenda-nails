package com.rafael.nailspro.webapp.application.retention;

import com.rafael.nailspro.webapp.application.messages.RetentionMessageBuilder;
import com.rafael.nailspro.webapp.application.whatsapp.WhatsappMessageService;
import com.rafael.nailspro.webapp.domain.enums.appointment.AppointmentStatus;
import com.rafael.nailspro.webapp.domain.enums.appointment.RetentionStatus;
import com.rafael.nailspro.webapp.domain.enums.evolution.EvolutionMessageStatus;
import com.rafael.nailspro.webapp.domain.enums.whatsapp.WhatsappMessageStatus;
import com.rafael.nailspro.webapp.domain.enums.whatsapp.WhatsappMessageType;
import com.rafael.nailspro.webapp.domain.model.*;
import com.rafael.nailspro.webapp.domain.repository.RetentionForecastRepository;
import com.rafael.nailspro.webapp.domain.whatsapp.SentMessageResult;
import com.rafael.nailspro.webapp.domain.whatsapp.WhatsappProvider;
import com.rafael.nailspro.webapp.infrastructure.dto.retention.RetentionData;
import com.rafael.nailspro.webapp.infrastructure.helper.TenantUrlProvider;
import com.rafael.nailspro.webapp.support.factory.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitPredictionServiceTest {

    @Mock
    private RetentionForecastRepository repository;
    @Mock
    private RetentionMessageBuilder messageBuilder;
    @Mock
    private WhatsappProvider whatsappProvider;
    @Mock
    private TransactionTemplate transactionTemplate;
    @Mock
    private WhatsappMessageService whatsappMessageService;
    @InjectMocks
    private VisitPredictionService visitPredictionService;

    void prepareTransaction() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(new SimpleTransactionStatus());
        });
        doAnswer(invocation -> {
            Consumer<TransactionStatus> consumer = invocation.getArgument(0);
            consumer.accept(new SimpleTransactionStatus());
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    private static Appointment standardAppointment() {
        Client client = TestUserFactory.client();
        Professional professional = TestProfessionalFactory.standard();
        SalonService salonService = TestSalonServiceFactory.standard();
        return TestAppointmentFactory.standard(client, professional, salonService);
    }

    private static Appointment standardAppointment(Client client) {
        Professional professional = TestProfessionalFactory.standard();
        SalonService salonService = TestSalonServiceFactory.standard();
        return TestAppointmentFactory.standard(client, professional, salonService);
    }

    @Test
    void createForecast_savesForecastSuccessfully_whenAppointmentValid() {
        Appointment appointment = standardAppointment();

        assertDoesNotThrow(() -> visitPredictionService.createForecast(appointment));
        verify(repository, times(1)).save(any(RetentionForecast.class));
    }

    @Test
    void createForecast_savesWithShortestInterval_whenMultipleServicesExist() {
        SalonService main = TestSalonServiceFactory.withInterval(30);
        SalonService addon = TestSalonServiceFactory.withInterval(15);

        Appointment appointment = TestAppointmentFactory.withAddOns(
                TestUserFactory.client(),
                TestProfessionalFactory.standard(),
                main,
                List.of(TestAppointmentAddOnFactory.standard(addon))
        );

        visitPredictionService.createForecast(appointment);

        ArgumentCaptor<RetentionForecast> captor = ArgumentCaptor.forClass(RetentionForecast.class);
        verify(repository).save(captor.capture());

        RetentionForecast saved = captor.getValue();

        long daysBetween = ChronoUnit.DAYS.between(
                appointment.getEndDate().atZone(ZoneOffset.UTC),
                saved.getPredictedReturnDate().atZone(ZoneOffset.UTC)
        );
        assertEquals(15, daysBetween, "Should use the shortest maintenance interval");
        assertEquals(2, saved.getSalonServices().size(), "Should contain both services");
    }

    @Test
    void createForecast_doesNotSave_whenNoServicesHaveIntervals() {
        SalonService main = TestSalonServiceFactory.withInterval(null);
        Appointment appointment = TestAppointmentFactory.standard(
                TestUserFactory.client(),
                TestProfessionalFactory.standard(),
                main
        );

        assertThrows(IllegalArgumentException.class, () -> visitPredictionService.createForecast(appointment));
        verify(repository, never()).save(any());
    }

    @Test
    void createForecast_throwsException_whenAppointmentInvalid() {
        Client client = TestUserFactory.client();
        Professional professional = TestProfessionalFactory.standard();
        Appointment appointment = TestAppointmentFactory.withNullMainService(client, professional, AppointmentStatus.FINISHED);

        assertThrows(IllegalArgumentException.class, () -> visitPredictionService.createForecast(appointment));
        verify(repository, times(0)).save(any(RetentionForecast.class));
    }

    @Test
    void markForecastAsExpired_updatesStatus_whenDateIsInPast() {
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        RetentionForecast forecast = RetentionForecast.builder()
                .predictedReturnDate(yesterday)
                .status(RetentionStatus.PENDING)
                .build();

        visitPredictionService.markForecastAsExpired(forecast);

        assertEquals(RetentionStatus.EXPIRED, forecast.getStatus());
    }

    @Test
    void markForecastAsExpired_throwsException_whenDateIsInFuture() {
        Instant tomorrow = Instant.now().plus(1, ChronoUnit.DAYS);
        RetentionForecast forecast = RetentionForecast.builder()
                .predictedReturnDate(tomorrow)
                .status(RetentionStatus.PENDING)
                .build();

        assertThrows(IllegalStateException.class,
                () -> visitPredictionService.markForecastAsExpired(forecast));
        assertEquals(RetentionStatus.PENDING, forecast.getStatus());
    }

    @Test
    void markForecastAsExpired_allowsExpiration_whenDateIsExactlyNow() {
        Instant now = Instant.now();
        RetentionForecast forecast = RetentionForecast.builder()
                .predictedReturnDate(now)
                .status(RetentionStatus.PENDING)
                .build();

        assertDoesNotThrow(() -> visitPredictionService.markForecastAsExpired(forecast));
        assertEquals(RetentionStatus.EXPIRED, forecast.getStatus());
    }

    @Test
    void markForecastAsExpired_doesNothing_whenAlreadyExpired() {
        Instant past = Instant.now().minus(5, ChronoUnit.DAYS);
        RetentionForecast forecast = RetentionForecast.builder()
                .predictedReturnDate(past)
                .status(RetentionStatus.EXPIRED)
                .build();

        visitPredictionService.markForecastAsExpired(forecast);

        assertEquals(RetentionStatus.EXPIRED, forecast.getStatus());
    }

    @Test
    void sendRetentionMaintenanceMessage_savesSuccessfully_whenForecastIsValid() {
        prepareTransaction();
        Long retentionForecastId = 1L;
        String tenantId = "tenant-test";
        String fakeMessage = "Mock Message";

        Appointment appointment = standardAppointment();

        RetentionForecast forecast = new RetentionForecast();
        forecast.setClient(appointment.getClient());
        forecast.setOriginAppointment(appointment);

        WhatsappMessage messageRecord = new WhatsappMessage();
        SentMessageResult successResult = new SentMessageResult("msg-id-123", EvolutionMessageStatus.PENDING);

        when(repository.findWithJoins(retentionForecastId)).thenReturn(Optional.of(forecast));
        when(whatsappMessageService.prepareRetentionMessage(anyLong(), any())).thenReturn(messageRecord);
        when(messageBuilder.buildRetentionMessage(any())).thenReturn(fakeMessage);
        when(whatsappProvider.sendText(eq(tenantId), eq(fakeMessage), eq(appointment.getClient().getPhoneNumber()))).thenReturn(successResult);

        visitPredictionService.sendRetentionMaintenanceMessage(retentionForecastId);

        verify(whatsappProvider).sendText(eq(tenantId), eq(fakeMessage), eq(appointment.getClient().getPhoneNumber()));
        verify(whatsappMessageService).updateMessageStatus(eq(WhatsappMessageStatus.PENDING), isNull(), eq("msg-id-123"), any());
    }

    @Test
    void sendRetentionMaintenanceMessage_throwsException_whenSendTextFails() {
        prepareTransaction();
        Long retentionForecastId = 1L;
        String tenantId = "tenant-test";
        String phoneNumber = "5511999999999";
        String fakeMessage = "Mock Message";

        Appointment appointment = standardAppointment();
        RetentionForecast forecast = new RetentionForecast();
        forecast.setClient(appointment.getClient());
        forecast.setOriginAppointment(appointment);

        WhatsappMessage messageRecord = new WhatsappMessage();

        when(repository.findWithJoins(retentionForecastId)).thenReturn(Optional.of(forecast));
        when(whatsappMessageService.prepareRetentionMessage(anyLong(), any())).thenReturn(messageRecord);
        when(messageBuilder.buildRetentionMessage(any())).thenReturn(fakeMessage);
        when(whatsappProvider.sendText(eq(tenantId), eq(fakeMessage), eq(phoneNumber))).thenThrow(HttpServerErrorException.class);

        visitPredictionService.sendRetentionMaintenanceMessage(retentionForecastId);

        verify(whatsappMessageService).updateMessageStatus(eq(WhatsappMessageStatus.FAILED), any(), isNull(), any());
    }

    @Test
    void sendRetentionMaintenanceMessage_throwsIllegalArgumentException_whenRetentionNotFound() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(new SimpleTransactionStatus());
        });

        Long id = 99L;
        when(repository.findWithJoins(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> visitPredictionService.sendRetentionMaintenanceMessage(id));

        verifyNoInteractions(whatsappProvider);
        verifyNoInteractions(whatsappMessageService);
    }

    @Test
    void sendRetentionMaintenanceMessage_handlesTimeout_correctly() {
        prepareTransaction();
        Long retentionForecastId = 1L;
        String timeoutError = "Read time out";

        Appointment appointment = standardAppointment();
        RetentionForecast forecast = new RetentionForecast();
        forecast.setClient(appointment.getClient());
        forecast.setOriginAppointment(appointment);

        WhatsappMessage messageRecord = new WhatsappMessage();

        when(repository.findWithJoins(retentionForecastId)).thenReturn(Optional.of(forecast));
        when(whatsappMessageService.prepareRetentionMessage(anyLong(), any())).thenReturn(messageRecord);
        when(messageBuilder.buildRetentionMessage(any())).thenReturn("msg");
        when(whatsappProvider.sendText(any(), any(), any())).thenThrow(new RuntimeException(timeoutError));

        visitPredictionService.sendRetentionMaintenanceMessage(retentionForecastId);

        verify(whatsappMessageService).updateMessageStatus(eq(WhatsappMessageStatus.FAILED), eq(timeoutError), isNull(), any());
    }
}