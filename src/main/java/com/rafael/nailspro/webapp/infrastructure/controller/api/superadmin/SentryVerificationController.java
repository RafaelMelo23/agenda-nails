package com.rafael.nailspro.webapp.infrastructure.controller.api.superadmin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('SUPER_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "SuperAdmin", description = "Super admin utilities")
public class SentryVerificationController {

    @GetMapping("/api/internal/debug-sentry")
    @Operation(summary = "Trigger Sentry error", description = "Throws and reports a test exception to Sentry.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Test exception sent"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public String triggerSentryError() {
        try {
            throw new RuntimeException("Sentry Verification Exception - Production Environment");
        } catch (Exception e) {

            io.sentry.Sentry.captureException(e);
            return "Test exception has been sent to Sentry.";
        }
    }
}

