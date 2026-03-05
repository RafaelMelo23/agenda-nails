package com.rafael.nailspro.webapp.application.appointment.message.schedule;

import com.rafael.nailspro.webapp.application.appointment.message.AppointmentMessagingUseCase;
import com.rafael.nailspro.webapp.domain.model.AppointmentNotification;
import com.rafael.nailspro.webapp.domain.repository.AppointmentNotificationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.rafael.nailspro.webapp.domain.enums.appointment.AppointmentNotificationStatus.FAILED;
import static com.rafael.nailspro.webapp.domain.enums.appointment.AppointmentNotificationType.CONFIRMATION;

@Service
@RequiredArgsConstructor
public class AppointmentConfirmationRetryableJob {

    private final AppointmentMessagingUseCase messagingUseCase;
    private final AppointmentNotificationRepository notificationRepository;
    private final EntityManagerFactory entityManagerFactory;
    // todo: consider changing to a circuit-breaker like approach for all scheduled sending message classes

    @Scheduled(cron = "0 */5 * * * *")
    public void retryFailedConfirmationMessages() {
        final int MAX_RETRIES = 3;

        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            Session session = entityManager.unwrap(Session.class);
            session.disableFilter("tenantFilter");

            List<AppointmentNotification> notifications =
                    notificationRepository.findRetriableMessages(MAX_RETRIES, FAILED, CONFIRMATION);

            notifications.forEach(no ->
                    messagingUseCase.processNotification(no.getAppointment().getId(), CONFIRMATION));
        }
    }
}