package com.rafael.agendanails.webapp.infrastructure.dto.appointment.notification;

import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Builder
public record AppointmentMessageInfoDTO(String tenantId,
                                        Long appointmentId,
                                        String professionalName,
                                        String salonTradeName,
                                        String clientName,
                                        ZonedDateTime zonedDateTime,
                                        String mainServiceName,
                                        Optional<List<String>> adjacentServices
) {
}
