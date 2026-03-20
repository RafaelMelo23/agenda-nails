package com.rafael.nailspro.webapp.application.retention;

import com.rafael.nailspro.webapp.application.messages.RetentionMessageBuilder;
import com.rafael.nailspro.webapp.application.whatsapp.WhatsappMessageService;
import com.rafael.nailspro.webapp.domain.model.Appointment;
import com.rafael.nailspro.webapp.domain.model.AppointmentAddOn;
import com.rafael.nailspro.webapp.domain.model.RetentionForecast;
import com.rafael.nailspro.webapp.domain.model.WhatsappMessage;
import com.rafael.nailspro.webapp.domain.repository.RetentionForecastRepository;
import com.rafael.nailspro.webapp.domain.enums.whatsapp.WhatsappMessageStatus;
import com.rafael.nailspro.webapp.domain.enums.whatsapp.WhatsappMessageType;
import com.rafael.nailspro.webapp.domain.whatsapp.SentMessageResult;
import com.rafael.nailspro.webapp.domain.whatsapp.WhatsappProvider;
import com.rafael.nailspro.webapp.infrastructure.dto.retention.RetentionData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.rafael.nailspro.webapp.domain.enums.appointment.RetentionStatus.*;

@Service
@RequiredArgsConstructor
public class VisitPredictionService {

    private final RetentionForecastRepository repository;
    private final RetentionMessageBuilder messageBuilder;
    private final WhatsappProvider whatsappProvider;
    private final WhatsappMessageService whatsappMessageService;
    private final TransactionTemplate transactionTemplate;

    @Transactional
    public void createForecast(Appointment appointment) {
        repository.save(RetentionForecast.create(appointment));
    }

    @Transactional
    public void markForecastAsExpired(RetentionForecast forecast) {
        if (forecast.getPredictedReturnDate().isAfter(Instant.now())) {
            throw new IllegalStateException("Can't expire an still active forecast");
        }
        forecast.setStatus(EXPIRED);
    }

    @Transactional
    public void markForecastAsConverted(RetentionForecast forecast) {
        forecast.setStatus(CONVERTED);
    }

    public void sendRetentionMaintenanceMessage(Long retentionForecastId) {
        RetentionData data = transactionTemplate.execute(status -> {
            RetentionForecast forecast = repository.findWithJoins(retentionForecastId)
                    .orElseThrow(() -> new IllegalArgumentException("Retention forecast not found with the ID: " + retentionForecastId));

            WhatsappMessage messageRecord = whatsappMessageService.prepareRetentionMessage(
                    retentionForecastId,
                    WhatsappMessageType.RETENTION_MAINTENANCE
            );

            String messageContent = messageBuilder.buildRetentionMessage(forecast);
            return new RetentionData(forecast, messageRecord, messageContent);
        });
        try {
            SentMessageResult result = whatsappProvider.sendText(
                    data.forecast().getTenantId(),
                    data.messageContent(),
                    data.forecast().getClient().getPhoneNumber()
            );

            transactionTemplate.executeWithoutResult(status ->
                    saveSuccessfulAttempt(result, data.messageRecord())
            );
        } catch (Exception e) {
            transactionTemplate.executeWithoutResult(status ->
                    handleFailedMessage(data.forecast(), data.messageRecord(), e)
            );
        }
    }

    private void saveSuccessfulAttempt(SentMessageResult result, WhatsappMessage messageRecord) {
        whatsappMessageService.updateMessageStatus(
                WhatsappMessageStatus.fromEvolutionStatus(result.status()),
                null,
                result.messageId(),
                messageRecord.getId()
        );
    }

    private void handleFailedMessage(RetentionForecast retentionForecast,
                                     WhatsappMessage messageRecord,
                                     Exception e) {
        retentionForecast.setStatus(FAILED_TO_SEND);
        whatsappMessageService.updateMessageStatus(
                WhatsappMessageStatus.FAILED,
                e.getMessage(),
                null,
                messageRecord.getId()
        );
    }
}