package com.rafael.nailspro.webapp.infrastructure.dto.whatsapp.evolution.text;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rafael.nailspro.webapp.domain.enums.evolution.EvolutionMessageStatus;

public record SendTextResponseDTO(
        @JsonProperty("key") Key key,
        @JsonProperty("status") EvolutionMessageStatus status,
        @JsonProperty("messageTimestamp") Long messageTimestamp,
        @JsonProperty("instanceId") String instanceId
) {
    public record Key(
            @JsonProperty("id") String id
    ) {}

    public String messageId() {
        return key != null ? key.id() : null;
    }
}