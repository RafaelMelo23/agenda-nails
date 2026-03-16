package com.rafael.nailspro.webapp.infrastructure.controller.api.professional;

import com.rafael.nailspro.webapp.application.professional.ProfessionalQueryService;
import com.rafael.nailspro.webapp.infrastructure.dto.professional.ProfessionalSimplifiedDTO;
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
@RequestMapping("/api/v1/professional")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Professional - Query", description = "Professional lookup")
public class ProfessionalQueryController {

    private final ProfessionalQueryService professionalQueryService;

    @Operation(summary = "List professionalsIds (simplified)", description = "Returns a simplified list of professionalsIds.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Professionals returned",
                    content = @Content(schema = @Schema(implementation = ProfessionalSimplifiedDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/simplified")
    public ResponseEntity<List<ProfessionalSimplifiedDTO>> getProfessionals() {

        return ResponseEntity.ok(professionalQueryService.findAllSimplified());
    }
}
