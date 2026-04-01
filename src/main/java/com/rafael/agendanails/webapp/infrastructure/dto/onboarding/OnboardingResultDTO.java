package com.rafael.agendanails.webapp.infrastructure.dto.onboarding;

import com.rafael.agendanails.webapp.domain.model.Professional;
import com.rafael.agendanails.webapp.domain.model.SalonProfile;
import lombok.Builder;

@Builder
public record OnboardingResultDTO(SalonProfile profile,
                                  Professional owner) {
}
