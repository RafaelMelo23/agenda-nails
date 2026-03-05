package com.rafael.nailspro.webapp.domain.enums.appointment;

import com.rafael.nailspro.webapp.domain.enums.evolution.EvolutionMessageStatus;

public enum AppointmentNotificationStatus {

    PENDING,
    SENT,
    DELIVERED,
    FAILED;

    public static AppointmentNotificationStatus fromEvolutionStatus(EvolutionMessageStatus value) {
        return switch(value) {
            case PENDING -> PENDING;
            case SERVER_ACK -> SENT;
            case DELIVERY_ACK -> DELIVERED;
            default -> FAILED;
        };
    }
}