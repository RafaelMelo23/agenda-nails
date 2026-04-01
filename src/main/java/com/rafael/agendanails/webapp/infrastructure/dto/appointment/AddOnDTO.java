package com.rafael.agendanails.webapp.infrastructure.dto.appointment;

public record AddOnDTO(
        Long serviceId,
        String serviceName,
        Integer quantity,
        Integer unitPriceSnapshot
) {}
