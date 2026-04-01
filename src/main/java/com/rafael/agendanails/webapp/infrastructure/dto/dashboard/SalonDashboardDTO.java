package com.rafael.agendanails.webapp.infrastructure.dto.dashboard;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record SalonDashboardDTO(
        BigDecimal monthlyRevenue,
        BigDecimal weeklyRevenue,
        Long monthlyAppointmentsCount,
        BigDecimal averageTicket,
        List<DailyRevenueDTO> chartData
) {
}
