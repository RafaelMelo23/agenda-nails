package com.rafael.agendanails.webapp.infrastructure.dto.appointment.booking.event;

import com.rafael.agendanails.webapp.domain.model.Appointment;

public record AppointmentConfirmedEvent(Appointment appointment) {
    public Long appointmentId() {
        return appointment.getId();
    }
}
