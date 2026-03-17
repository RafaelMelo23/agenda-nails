package com.rafael.nailspro.webapp.infrastructure.controller.api.salon.service;

import com.rafael.nailspro.webapp.application.salon.business.SalonServiceService;
import com.rafael.nailspro.webapp.infrastructure.dto.salon.service.SalonServiceOutDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/salon/service")
@Tag(name = "Salon Service", description = "Salon Service List")
public class SalonServiceQueryController {

    private final SalonServiceService salonService;

    @Operation(summary = "List salon services", description = "Returns all salon services.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Services returned",
                    content = @Content(schema = @Schema(implementation = SalonServiceOutDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public ResponseEntity<List<SalonServiceOutDTO>> getAllSalonServices() {
        return ResponseEntity.ok(salonService.getServices());
    }
}