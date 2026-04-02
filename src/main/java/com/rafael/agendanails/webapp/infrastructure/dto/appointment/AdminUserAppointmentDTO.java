package com.rafael.agendanails.webapp.infrastructure.dto.appointment;

import com.rafael.agendanails.webapp.domain.enums.appointment.AppointmentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record AdminUserAppointmentDTO(

        Long appointmentId,

        Long clientId,
        String clientName,
        String clientPhoneNumber,
        Integer clientMissedAppointments,
        Integer clientCanceledAppointments,

        Long professionalId,
        String professionalName,

        Long mainServiceId,
        String mainServiceName,
        Integer mainServiceDurationInSeconds,
        Integer mainServiceValue,

        List<AddOnDTO> addOns,

        AppointmentStatus status,
        BigDecimal totalValue,
        String observations,
        ZonedDateTime startDateAndTime,
        ZonedDateTime endDateAndTime
) {
}

