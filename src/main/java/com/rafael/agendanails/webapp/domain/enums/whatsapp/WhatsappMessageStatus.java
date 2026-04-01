package com.rafael.agendanails.webapp.domain.enums.whatsapp;

import com.rafael.agendanails.webapp.domain.enums.evolution.EvolutionMessageStatus;

public enum WhatsappMessageStatus {

    PENDING,
    SENT,
    DELIVERED,
    FAILED;

    public static WhatsappMessageStatus fromEvolutionStatus(EvolutionMessageStatus value) {
        return switch (value) {
            case PENDING -> PENDING;
            case SERVER_ACK -> SENT;
            case DELIVERY_ACK, READ, PLAYED -> DELIVERED;
            default -> FAILED;
        };
    }
}
