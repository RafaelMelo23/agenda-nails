package com.rafael.nailspro.webapp.infrastructure.controller.api.admin.salon;

import com.rafael.nailspro.webapp.application.admin.salon.profile.SalonProfileManagementService;
import com.rafael.nailspro.webapp.domain.model.UserPrincipal;
import com.rafael.nailspro.webapp.infrastructure.config.SwaggerExamples;
import com.rafael.nailspro.webapp.infrastructure.dto.admin.salon.profile.SalonProfileDTO;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/salon/profile")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Salon", description = "Salon profile management")
public class SalonProfileController {

    private final SalonProfileManagementService salonProfileManagementService;

    @Operation(summary = "Update salon profile", description = "Updates the salon profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SalonProfileDTO.class),
                    examples = @ExampleObject(name = "SalonProfileRequest", value = SwaggerExamples.SALON_PROFILE_REQUEST))
    )
    @PatchMapping
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal UserPrincipal user,
                                              @Valid @RequestBody SalonProfileDTO dto) throws IOException {

        salonProfileManagementService.updateProfile(user.getUserId(), dto);
        return ResponseEntity.ok().build();
    }
}
