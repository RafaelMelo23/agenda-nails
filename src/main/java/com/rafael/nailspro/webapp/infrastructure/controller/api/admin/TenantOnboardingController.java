package com.rafael.nailspro.webapp.infrastructure.controller.api.admin;

import com.rafael.nailspro.webapp.application.salon.business.OnboardingService;
import com.rafael.nailspro.webapp.infrastructure.dto.onboarding.OnboardingRequestDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.onboarding.OnboardingResultDTO;
import com.rafael.nailspro.webapp.infrastructure.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/onboard")
public class TenantOnboardingController {

    @Value("${internal.api.key}")
    private String key;

    private final OnboardingService onboardingService;

    @PostMapping
    public ResponseEntity<OnboardingResultDTO> onboardNewClient(@Valid @RequestBody OnboardingRequestDTO dto,
                                                                @RequestHeader ("X-API-Key") String apiKey) {

        if (!key.equals(apiKey)) throw new BusinessException("Invalid API Key");

        return ResponseEntity.ok(onboardingService.onboardOwner(dto));
    }
}