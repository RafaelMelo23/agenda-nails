package com.rafael.agendanails.webapp.infrastructure.controller.api.evolution;

import com.rafael.agendanails.webapp.application.whatsapp.ManageWhatsappConnectionUseCase;
import com.rafael.agendanails.webapp.domain.enums.evolution.WhatsappConnectionMethod;
import com.rafael.agendanails.webapp.domain.model.UserPrincipal;
import com.rafael.agendanails.webapp.infrastructure.dto.whatsapp.WhatsappConnectionResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/whatsapp")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "WhatsApp", description = "WhatsApp connection management")
public class WhatsappController {

    private final ManageWhatsappConnectionUseCase whatsappConnectionUseCase;

    @Operation(summary = "Setup WhatsApp connection", description = "Creates a WhatsApp connection using QR code or pairing code.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connection initiated"),
            @ApiResponse(responseCode = "400", description = "Invalid connection method"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<WhatsappConnectionResponseDTO> setupConnection(@RequestParam @NotNull(message = "O método de conexão é obrigatório")
                                                @Parameter(example = "QR_CODE")
                                                WhatsappConnectionMethod connectionMethod,
                                                @AuthenticationPrincipal UserPrincipal userPrincipal) {

        WhatsappConnectionResponseDTO response = whatsappConnectionUseCase.setupInitialConnection(userPrincipal.getTenantId(), connectionMethod);
        return ResponseEntity.ok(response);
    }
}