package com.rafael.agendanails.webapp.domain.enums.evolution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EvolutionMessageStatus {

    PENDING,
    SERVER_ACK,
    DELIVERY_ACK,
    READ,
    PLAYED;

    @JsonCreator
    public static EvolutionMessageStatus fromString(String value) {
        if (value == null) {
            return null;
        }

        return EvolutionMessageStatus.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}