package com.rafael.nailspro.webapp.infrastructure.dto.whatsapp.evolution.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rafael.nailspro.webapp.domain.enums.evolution.EvolutionWebhookEvent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EvolutionWebhookResponseDTO<T>(

        @NotNull(message = "O evento do webhook é obrigatório.")
        EvolutionWebhookEvent event,

        @NotBlank(message = "A instância do webhook é obrigatória.")
        String instance,

        @NotNull(message = "O payload 'data' do webhook é obrigatório.")
        @Valid
        T data,

        @NotBlank(message = "O destino do webhook é obrigatório.")
        String destination,

        @NotBlank(message = "A data e hora do webhook são obrigatórias.")
        @Pattern(
                regexp = "^\\d{4}-\\d{2}-\\d{2}.*",
                message = "A data e hora do webhook devem estar em um formato válido."
        )
        @JsonProperty("date_time")
        String dateTime,

        @NotBlank(message = "A URL do servidor é obrigatória.")
        @JsonProperty("server_url")
        String serverUrl,

        @NotBlank(message = "O remetente do webhook é obrigatório.")
        String sender,

        @NotBlank(message = "A chave de API é obrigatória.")
        String apikey

) {}