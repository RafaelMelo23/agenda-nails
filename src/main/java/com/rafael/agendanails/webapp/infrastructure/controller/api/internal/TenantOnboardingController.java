package com.rafael.agendanails.webapp.infrastructure.controller.api.internal;

import com.rafael.agendanails.webapp.application.internal.OnboardingService;
import com.rafael.agendanails.webapp.application.internal.TenantManagementService;
import com.rafael.agendanails.webapp.domain.enums.appointment.TenantStatus;
import com.rafael.agendanails.webapp.infrastructure.config.SwaggerExamples;
import com.rafael.agendanails.webapp.infrastructure.dto.onboarding.OnboardingRequestDTO;
import com.rafael.agendanails.webapp.infrastructure.dto.onboarding.OnboardingResultDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/onboard/tenant")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Internal - Onboarding", description = "Tenant onboarding and status management")
public class TenantOnboardingController {

    private final OnboardingService onboardingService;
    private final TenantManagementService tenantManagementService;

    @Operation(summary = "Onboard tenant", description = "Onboards a new tenant and owner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant onboarded",
                    content = @Content(schema = @Schema(implementation = OnboardingResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OnboardingRequestDTO.class),
                    examples = @ExampleObject(name = "OnboardingRequest", value = SwaggerExamples.ONBOARDING_REQUEST))
    )
    @PostMapping()
    public ResponseEntity<OnboardingResultDTO> onboardNewClient(@Valid @RequestBody OnboardingRequestDTO dto) {

        return ResponseEntity.ok(onboardingService.onboardOwner(dto));
    }

    @Operation(summary = "Update tenant status", description = "Updates a tenant status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid tenant id or status"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/{tenantId}/status")
    public ResponseEntity<Boolean> manageTenantStatus(
                                                      @Parameter(example = "tenant-test")
                                                      @PathVariable String tenantId,
                                                      @Parameter(example = "ACTIVE")
                                                      @RequestParam TenantStatus status) {

        return ResponseEntity.ok(tenantManagementService.updateTenantStatus(tenantId, status));
    }
}
