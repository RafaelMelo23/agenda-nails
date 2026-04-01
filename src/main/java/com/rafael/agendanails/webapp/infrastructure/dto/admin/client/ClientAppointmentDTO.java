package com.rafael.agendanails.webapp.infrastructure.dto.admin.client;

import com.rafael.agendanails.webapp.domain.enums.appointment.AppointmentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record ClientAppointmentDTO(
        Long appointmentId,
        Long professionalId,
        String professionalName,

        ZonedDateTime startDateAndTime,
        ZonedDateTime endDateAndTime,
        AppointmentStatus status,

        String mainServiceName,
        List<String> addOnServiceNames,
        BigDecimal totalValue,

        String observations
) {
}