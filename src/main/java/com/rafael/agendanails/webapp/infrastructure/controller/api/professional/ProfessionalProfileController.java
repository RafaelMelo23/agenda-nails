package com.rafael.agendanails.webapp.infrastructure.controller.api.professional;

import com.rafael.agendanails.webapp.application.professional.ProfessionalProfileManagementUseCase;
import com.rafael.agendanails.webapp.infrastructure.config.SwaggerExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/professional/profile")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Professional - Profile", description = "Professional profile management")
public class ProfessionalProfileController {

    private final ProfessionalProfileManagementUseCase profileService;

    @Operation(summary = "Update profile picture", description = "Updates the professional profile picture.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profile picture updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class),
                    examples = @ExampleObject(name = "ProfilePictureBase64", value = SwaggerExamples.PROFILE_PICTURE_BASE64))
    )
    @PatchMapping("/{id}/picture")
    public ResponseEntity<Void> updatePicture(
            @io.swagger.v3.oas.annotations.Parameter(example = "2002")
            @PathVariable Long id,
            @RequestBody String pictureBase64
    ) throws IOException {
        profileService.updateProfilePicture(id, pictureBase64);
        return ResponseEntity.noContent().build();
    }
}
