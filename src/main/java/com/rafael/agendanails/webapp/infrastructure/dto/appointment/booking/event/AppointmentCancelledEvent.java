package com.rafael.agendanails.webapp.infrastructure.dto.appointment.booking.event;

public record AppointmentCancelledEvent(
        Long appointmentId,
        String tenantId,
        Long clientId
) {
}

