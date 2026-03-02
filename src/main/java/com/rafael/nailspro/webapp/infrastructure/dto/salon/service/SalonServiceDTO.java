package com.rafael.nailspro.webapp.infrastructure.dto.salon.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SalonServiceDTO(

        Long id,

        @NotBlank(message = "O nome do serviço é obrigatório")
        String name,

        @NotNull(message = "O valor do serviço é obrigatório")
        @Positive(message = "O valor do serviço deve ser maior que zero")
        Integer value,

        @NotNull(message = "A duração do serviço é obrigatória")
        @Positive(message = "A duração do serviço deve ser maior que zero")
        Integer durationInSeconds,

        @NotBlank(message = "A descrição do serviço é obrigatória")
        String description,

        @Positive(message = "O intervalo de manutenção deve ser maior que zero")
        Integer maintenanceIntervalDays,

        Boolean requiresLoyalty,
        Boolean isAddOn,
        Optional<List<Long>> professionals
) {}