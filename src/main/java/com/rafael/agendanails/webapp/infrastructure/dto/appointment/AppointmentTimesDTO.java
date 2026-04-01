package com.rafael.agendanails.webapp.infrastructure.dto.appointment;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
public record AppointmentTimesDTO(LocalDate date,
                                  List<LocalTime> availableTimes) {
}
