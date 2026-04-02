package com.rafael.agendanails.webapp.infrastructure.dto.whatsapp.evolution.webhook.message.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rafael.agendanails.webapp.domain.enums.evolution.EvolutionMessageStatus;

public record MessageUpdateData(
        @JsonProperty("keyId")
        String messageId,
        EvolutionMessageStatus status
) {}