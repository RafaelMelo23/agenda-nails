package com.rafael.nailspro.webapp.infrastructure.controller.api.internal;

import com.rafael.nailspro.webapp.application.internal.OnboardingService;
import com.rafael.nailspro.webapp.application.internal.TenantManagementService;
import com.rafael.nailspro.webapp.domain.enums.appointment.TenantStatus;
import com.rafael.nailspro.webapp.infrastructure.dto.onboarding.OnboardingRequestDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.onboarding.OnboardingResultDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/onboard/tenant")
public class TenantOnboardingController {

    private final OnboardingService onboardingService;
    private final TenantManagementService tenantManagementService;

    @PostMapping()
    public ResponseEntity<OnboardingResultDTO> onboardNewClient(@Valid @RequestBody OnboardingRequestDTO dto) {

        return ResponseEntity.ok(onboardingService.onboardOwner(dto));
    }

    @PostMapping("/{tenantId}/status")
    public ResponseEntity<Boolean> manageTenantStatus(@PathVariable String tenantId,
                                                   @RequestParam TenantStatus status) {

        return ResponseEntity.ok(tenantManagementService.updateTenantStatus(tenantId, status));
    }
}