package com.rafael.nailspro.webapp.application.appointment.message;

import com.rafael.nailspro.webapp.domain.enums.appointment.AppointmentNotificationStatus;
import com.rafael.nailspro.webapp.domain.enums.appointment.AppointmentNotificationType;
import com.rafael.nailspro.webapp.domain.model.AppointmentNotification;
import com.rafael.nailspro.webapp.domain.repository.AppointmentNotificationRepository;
import com.rafael.nailspro.webapp.domain.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AppointmentNotificationService {

    private final AppointmentNotificationRepository notificationRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public AppointmentNotification prepareNotification(Long appointmentId,
                                                       AppointmentNotificationType type) {

        return notificationRepository
                .findByAppointmentIdAndNotificationType(appointmentId, type)
                .orElseGet(() -> {
                    var appointment = appointmentRepository.findById(appointmentId)
                            .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

                    var newNotif = AppointmentNotification.builder()
                            .appointment(appointment)
                            .destinationNumber(appointment.getClient().getPhoneNumber())
                            .notificationType(type)
                            .notificationStatus(AppointmentNotificationStatus.FAILED)
                            .tenantId(appointment.getTenantId())
                            .attempts(0)
                            .build();

                    return notificationRepository.save(newNotif);
                });
    }

    @Transactional
    public void updateNotificationStatus(AppointmentNotificationStatus status,
                                         String error,
                                         String externalMessageId,
                                         Long id) {

        notificationRepository.findById(id).ifPresent(n -> {
            n.setNotificationStatus(status);
            n.setExternalMessageId(externalMessageId);
            n.setAttempts(n.getAttempts() + 1);
            n.setLastAttemptAt(Instant.now());
            n.setLastErrorMessage(error);
            if (status == AppointmentNotificationStatus.SENT) {
                n.setSentAt(Instant.now());
            }
        });
    }
}