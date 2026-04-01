package com.rafael.agendanails.webapp.infrastructure.controller.api.admin.salon;

import com.rafael.agendanails.webapp.application.salon.business.SalonServiceService;
import com.rafael.agendanails.webapp.infrastructure.config.SwaggerExamples;
import com.rafael.agendanails.webapp.infrastructure.dto.salon.service.SalonServiceDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/salon/service")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Salon", description = "Salon service management")
public class SalonServiceManagementController {

    private final SalonServiceService salonService;

    @Operation(summary = "Create salon service", description = "Creates a new salon service.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Service created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SalonServiceDTO.class),
                    examples = @ExampleObject(name = "SalonServiceRequest", value = SwaggerExamples.SALON_SERVICE_REQUEST))
    )
    @PostMapping
    public ResponseEntity<Void> createSalonService(@Valid @RequestBody SalonServiceDTO salonServiceDTO) {

        salonService.createService(salonServiceDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Change service visibility", description = "Toggles service visibility (active/inactive).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Visibility updated"),
            @ApiResponse(responseCode = "400", description = "Invalid service id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/active/{serviceId}/{isActive}")
    public ResponseEntity<Void> changeSalonServiceVisibility(
            @Parameter(example = "1001")
            @PathVariable Long serviceId,
            @Parameter(example = "true")
            @PathVariable Boolean isActive) {

        salonService.changeSalonServiceVisibility(serviceId, isActive);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update service", description = "Updates a salon service.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Service updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SalonServiceDTO.class),
                    examples = @ExampleObject(name = "SalonServiceRequest", value = SwaggerExamples.SALON_SERVICE_REQUEST))
    )
    @PatchMapping("/{serviceId}")
    public ResponseEntity<Void> updateSalonService(
            @Valid @RequestBody SalonServiceDTO salonServiceDTO,
            @Parameter(example = "1001")
            @PathVariable Long serviceId) {

        salonService.updateSalonService(serviceId, salonServiceDTO);
        return ResponseEntity.noContent().build();
    }
}
