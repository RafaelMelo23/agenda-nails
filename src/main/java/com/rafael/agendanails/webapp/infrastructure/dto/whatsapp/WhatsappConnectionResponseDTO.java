package com.rafael.agendanails.webapp.infrastructure.dto.whatsapp;

import com.rafael.agendanails.webapp.domain.enums.evolution.WhatsappConnectionMethod;

public record WhatsappConnectionResponseDTO(
        WhatsappConnectionMethod method,
        String pairingCode
) {
    public static WhatsappConnectionResponseDTO of(WhatsappConnectionMethod method, String pairingCode) {
        return new WhatsappConnectionResponseDTO(method, pairingCode);
    }
}