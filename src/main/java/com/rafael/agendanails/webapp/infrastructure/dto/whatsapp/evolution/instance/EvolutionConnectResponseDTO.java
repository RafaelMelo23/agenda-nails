package com.rafael.agendanails.webapp.infrastructure.dto.whatsapp.evolution.instance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EvolutionConnectResponseDTO(
        String pairingCode
) {}
