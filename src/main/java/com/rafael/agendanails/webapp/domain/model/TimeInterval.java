package com.rafael.agendanails.webapp.domain.model;

import com.rafael.agendanails.webapp.infrastructure.exception.BusinessException;
import lombok.Builder;

import java.time.*;

@Builder
public record TimeInterval(Instant realTimeStart,
                           Instant realTimeEnd,
                           Instant endTimeWithBuffer,
                           ZoneId salonZoneId) {

    public TimeInterval {
        if (realTimeEnd.isBefore(realTimeStart)) {
            throw new BusinessException("O horário de término deve ser após o início.");
        }
    }

    public LocalDateTime toLocalDateTime(Instant instant) {

        return LocalDateTime.ofInstant(instant, salonZoneId);
    }

    public LocalTime getStartTimeOnly() {
        return realTimeStart.atZone(salonZoneId).toLocalTime();
    }

    public LocalTime getEndTimeOnly() {
        return realTimeStart.atZone(salonZoneId).toLocalTime();
    }

    public LocalTime getEndTimeWithBuffer() {
        return realTimeStart.atZone(salonZoneId).toLocalTime();
    }

    public DayOfWeek getDayOfWeek() {
        return realTimeStart.atZone(salonZoneId).getDayOfWeek();
    }
}
