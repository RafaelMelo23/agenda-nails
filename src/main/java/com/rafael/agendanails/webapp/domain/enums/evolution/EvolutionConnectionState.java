package com.rafael.agendanails.webapp.domain.enums.evolution;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EvolutionConnectionState {

    CONNECTING,
    OPEN,
    CLOSE;

    @JsonCreator
    public static EvolutionConnectionState from(String value) {
        return EvolutionConnectionState.valueOf(value.toUpperCase());
    }
}
