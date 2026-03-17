package com.rafael.nailspro.webapp.infrastructure.controller.api.admin.client;

import com.rafael.nailspro.webapp.application.admin.dashboard.ClientInsightService;
import com.rafael.nailspro.webapp.infrastructure.dto.client.AdminClientCrmDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/insight/clients")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Clients", description = "Client insights and CRM data")
public class ClientInsightController {

    private final ClientInsightService service;

    @Operation(summary = "Get client CRM info", description = "Returns audited CRM information for a client.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CRM info returned",
                    content = @Content(schema = @Schema(implementation = AdminClientCrmDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid client id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{clientId}")
    public ResponseEntity<AdminClientCrmDTO> getClientDashboardInfo(
            @Parameter(example = "2001")
            @PathVariable @Positive(message = "O identificador do cliente deve ser positivo") Long clientId) {

        AdminClientCrmDTO response = service.getClientsAuditedInfo(clientId);

        return ResponseEntity.ok(response);
    }
}
