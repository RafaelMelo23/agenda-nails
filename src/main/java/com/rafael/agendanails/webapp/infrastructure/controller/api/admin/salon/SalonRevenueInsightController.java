package com.rafael.agendanails.webapp.infrastructure.controller.api.admin.salon;

import com.rafael.agendanails.webapp.application.admin.dashboard.SalonRevenueInsightUseCase;
import com.rafael.agendanails.webapp.domain.model.UserPrincipal;
import com.rafael.agendanails.webapp.infrastructure.dto.dashboard.SalonDashboardDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/insight/salon")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Salon", description = "Salon revenue insights")
public class SalonRevenueInsightController {

    private final SalonRevenueInsightUseCase salonRevenueUseCase;

    @Operation(summary = "Get monthly revenue", description = "Returns the salon monthly revenue dashboard.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Revenue returned",
                    content = @Content(schema = @Schema(implementation = SalonDashboardDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/revenue")
    public ResponseEntity<SalonDashboardDTO> getMonthlyRevenue(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        SalonDashboardDTO dashboard = salonRevenueUseCase.getMonthlySalonRevenue(userPrincipal.getTenantId());
        return ResponseEntity.ok(dashboard);
    }
}