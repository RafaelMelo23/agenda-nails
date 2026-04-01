package com.rafael.agendanails.webapp.infrastructure.dto.professional;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ProfessionalResponseDTO(
        Long id,
        UUID externalId,
        String name,
        String email,
        String professionalPicture,
        Boolean isActive,
        Boolean isFirstLogin
) {}