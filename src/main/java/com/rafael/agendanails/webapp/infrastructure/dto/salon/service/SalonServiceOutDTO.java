package com.rafael.agendanails.webapp.infrastructure.dto.salon.service;

import com.rafael.agendanails.webapp.infrastructure.dto.professional.ProfessionalSimplifiedDTO;
import lombok.Builder;

import java.util.Set;

@Builder
public record SalonServiceOutDTO(Long id,
                                 String name,
                                 Integer value,
                                 Integer durationInSeconds,
                                 String description,
                                 Set<ProfessionalSimplifiedDTO> professionals,
                                 Boolean isActive,
                                 Boolean isAddOn,
                                 Integer nailCount) {
}
