package com.rafael.nailspro.webapp.application.appointment.message.schedule;

import com.rafael.nailspro.webapp.domain.repository.AppointmentNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.rafael.nailspro.webapp.domain.enums.appointment.AppointmentNotificationStatus.SENT;

@Log4j2
@Service
@RequiredArgsConstructor
public class AppointmentNotificationCleanupJob {

    private final AppointmentNotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 5 * * *")
    public void deleteSentNotifications() {
        Instant twentyForHoursAgo = Instant.now().minus(24, ChronoUnit.HOURS);
        notificationRepository.deleteByStatusAndSentAtSmallerThanInBatch(SENT, twentyForHoursAgo);
    }
}