package com.rafael.agendanails.webapp.infrastructure.dto.professional;

import lombok.Builder;

import java.util.List;

@Builder
public record FindProfessionalAvailabilityDTO(String professionalExternalId,
                                              int serviceDurationInSeconds,
                                              List<Long> servicesIds

) {
}

