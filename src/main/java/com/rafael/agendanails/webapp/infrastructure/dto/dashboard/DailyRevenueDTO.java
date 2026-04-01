package com.rafael.agendanails.webapp.infrastructure.dto.dashboard;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record DailyRevenueDTO(
        LocalDate date,
        BigDecimal value,
        Long appointmentsCount
) {
}