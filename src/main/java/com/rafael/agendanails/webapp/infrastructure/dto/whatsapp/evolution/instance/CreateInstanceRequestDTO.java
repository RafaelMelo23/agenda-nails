package com.rafael.agendanails.webapp.infrastructure.dto.whatsapp.evolution.instance;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rafael.agendanails.webapp.domain.enums.evolution.EvolutionIntegraton;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateInstanceRequestDTO(
        String instanceName,
        boolean qrcode,
        EvolutionIntegraton integration,
        WebhookDTO webhook,
        String number
) {
}