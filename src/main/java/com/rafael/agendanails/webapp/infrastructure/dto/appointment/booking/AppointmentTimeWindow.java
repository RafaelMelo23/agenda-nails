package com.rafael.agendanails.webapp.infrastructure.dto.appointment.booking;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AppointmentTimeWindow(LocalDate start, LocalDate end) {
}
