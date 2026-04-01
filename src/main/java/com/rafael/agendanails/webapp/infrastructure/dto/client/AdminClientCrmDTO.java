package com.rafael.agendanails.webapp.infrastructure.dto.client;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Builder
public record AdminClientCrmDTO(
        Long clientId,
        String name,
        String phoneNumber,
        BigDecimal totalSpent,
        Long completedAppointments,
        Long canceledAppointments,
        Long missedAppointments,
        ZonedDateTime lastVisitDate
) {
}

