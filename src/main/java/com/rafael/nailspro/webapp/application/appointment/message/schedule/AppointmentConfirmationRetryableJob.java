package com.rafael.nailspro.webapp.application.appointment.message.schedule;

import com.rafael.nailspro.webapp.application.appointment.message.AppointmentMessagingUseCase;
import com.rafael.nailspro.webapp.domain.model.WhatsappMessage;
import com.rafael.nailspro.webapp.domain.repository.WhatsappMessageRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.rafael.nailspro.webapp.domain.enums.whatsapp.WhatsappMessageStatus.FAILED;
import static com.rafael.nailspro.webapp.domain.enums.whatsapp.WhatsappMessageType.CONFIRMATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentConfirmationRetryableJob {

    private final AppointmentMessagingUseCase messagingUseCase;
    private final WhatsappMessageRepository messageRepository;
    private final EntityManager entityManager;
    // todo: consider changing to a circuit-breaker like approach for all scheduled sending message classes

    @Scheduled(cron = "0 */5 * * * *")
    public void retryFailedConfirmationMessages() {
        final int MAX_RETRIES = 3;
        try {
            Session session = entityManager.unwrap(Session.class);
            session.disableFilter("tenantFilter");

            List<WhatsappMessage> messages =
                    messageRepository.findRetriableMessages(MAX_RETRIES, FAILED, CONFIRMATION);

            messages.forEach(message ->
                    messagingUseCase.processNotification(message.getAppointment().getId(), CONFIRMATION));
        } catch (Exception e) {
            log.error("Failed to batch process failed confirmation messages");
        }
    }
}