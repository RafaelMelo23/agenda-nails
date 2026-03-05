package com.rafael.nailspro.webapp.domain.repository;

import com.rafael.nailspro.webapp.domain.enums.appointment.AppointmentNotificationStatus;
import com.rafael.nailspro.webapp.domain.enums.appointment.AppointmentNotificationType;
import com.rafael.nailspro.webapp.domain.model.Appointment;
import com.rafael.nailspro.webapp.domain.model.AppointmentNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AppointmentNotificationRepository extends JpaRepository<AppointmentNotification, Long> {


    @Query("SELECT an FROM AppointmentNotification an WHERE an.appointment.id IN :appointments")
    List<AppointmentNotification> findByAppointments(@Param("appointments") List<Appointment> appointmentId);

    List<AppointmentNotification> findAllByNotificationStatusAndNotificationType(AppointmentNotificationStatus notificationStatus, AppointmentNotificationType notificationType);

    @Modifying
    @Transactional
    @Query("""
    DELETE FROM AppointmentNotification ap
    WHERE ap.notificationStatus = :status
    AND ap.sentAt < :instant""")
    void deleteByStatusAndSentAtSmallerThanInBatch(@Param("status") AppointmentNotificationStatus status,
                                                   @Param("instant") Instant instant);

    @Query("""
    SELECT ap FROM AppointmentNotification ap
    WHERE ap.attempts <= :maxRetries
    AND ap.notificationStatus = :status
    AND ap.notificationType = :type""")
    List<AppointmentNotification> findRetriableMessages(@Param("maxRetries") int maxRetries,
                                                        @Param("status") AppointmentNotificationStatus status,
                                                        @Param("type") AppointmentNotificationType type);

    Optional<AppointmentNotification> findByAppointmentIdAndNotificationType(Long appointmentId, AppointmentNotificationType type);

    boolean existsAppointmentNotificationByExternalMessageId(String externalMessageId);

    Optional<AppointmentNotification> findByExternalMessageId(String externalMessageId);
}