package com.rafael.nailspro.webapp.infrastructure.controller.api.admin.client;

import com.rafael.nailspro.webapp.application.admin.client.ClientManagementService;
import com.rafael.nailspro.webapp.domain.enums.user.UserStatus;
import com.rafael.nailspro.webapp.infrastructure.dto.admin.client.ClientAppointmentDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.admin.client.ClientDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/client")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Clients", description = "Client management")
public class ClientManagementController {

    private final ClientManagementService clientManagementService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update client status", description = "Updates a client's status (ACTIVE/BANNED).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid client id or status"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/status/{userId}/{userStatus}")
    public ResponseEntity<Void> updateClientStatus(
            @Parameter(example = "2001")
            @PathVariable @Positive(message = "O identificador do usuário deve ser positivo") Long userId,
            @Parameter(example = "ACTIVE")
            @PathVariable UserStatus userStatus) {

        clientManagementService.updateClientStatus(userId, userStatus);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search clients", description = "Searches clients by name with pagination.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clients returned",
                    content = @Content(schema = @Schema(implementation = ClientDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public ResponseEntity<Page<ClientDTO>> searchClients(
            @Parameter(example = "Maria")
            @RequestParam(required = false, defaultValue = "") String name,
            Pageable pageable) {

        Page<ClientDTO> clients =
                clientManagementService.findClientsForManagement(name, pageable);

        return ResponseEntity.ok(clients);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Client appointment history", description = "Returns appointment history for a client.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Appointments returned",
                    content = @Content(schema = @Schema(implementation = ClientAppointmentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid client id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{clientId}/appointments")
    public ResponseEntity<Page<ClientAppointmentDTO>> getClientAppointments(
            @Parameter(example = "2001")
            @PathVariable @Positive(message = "O identificador do cliente deve ser positivo") Long clientId,
            Pageable pageable) {

        Page<ClientAppointmentDTO> appointments =
                clientManagementService.getClientAppointmentHistory(clientId, pageable);

        return ResponseEntity.ok(appointments);
    }


}
