package com.rafael.agendanails.webapp.infrastructure.controller.api.professional;

import com.rafael.agendanails.webapp.application.professional.ProfessionalAppointmentStatusUseCase;
import com.rafael.agendanails.webapp.application.professional.ProfessionalScheduleQueryUseCase;
import com.rafael.agendanails.webapp.domain.model.UserPrincipal;
import com.rafael.agendanails.webapp.infrastructure.dto.appointment.ProfessionalAppointmentScheduleDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROFESSIONAL')")
@RequestMapping("/api/v1/professional/appointments")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Professional - Appointments", description = "Professional appointment management")
public class ProfessionalAppointmentManagementController {

    private final ProfessionalScheduleQueryUseCase professionalScheduleQueryUseCase;
    private final ProfessionalAppointmentStatusUseCase professionalAppointmentStatusUseCase;

    @Operation(summary = "List appointments by day", description = "Returns appointments for the professional in a date range.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Appointments returned",
                    content = @Content(schema = @Schema(implementation = ProfessionalAppointmentScheduleDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid dates"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public ResponseEntity<List<ProfessionalAppointmentScheduleDTO>> findByDay(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @NotNull(message = "A data e hora inicial são obrigatórias")
            @Parameter(example = "2026-04-01T00:00:00-03:00")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
            @RequestParam @NotNull(message = "A data e hora final são obrigatórias")
            @Parameter(example = "2026-04-01T23:59:59-03:00")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end
    ) {
        return ResponseEntity.ok(
                professionalScheduleQueryUseCase.findProfessionalAppointmentsByDay(
                        principal.getUserId(),
                        start,
                        end
                )
        );
    }

    @Operation(summary = "Confirm appointment", description = "Confirms an appointment.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Appointment confirmed"),
            @ApiResponse(responseCode = "400", description = "Invalid appointment id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{appointmentId}/confirm")
    public ResponseEntity<Void> confirmAppointment(
            @Parameter(example = "3001")
            @PathVariable @Positive(message = "O identificador do agendamento deve ser positivo") Long appointmentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        professionalAppointmentStatusUseCase.confirm(appointmentId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Finish appointment", description = "Marks an appointment as finished.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Appointment finished"),
            @ApiResponse(responseCode = "400", description = "Invalid appointment id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{appointmentId}/finish")
    public ResponseEntity<Void> finishAppointment(
            @Parameter(example = "3001")
            @PathVariable @Positive(message = "O identificador do agendamento deve ser positivo") Long appointmentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        professionalAppointmentStatusUseCase.finish(appointmentId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cancel appointment", description = "Cancels an appointment.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Appointment cancelled"),
            @ApiResponse(responseCode = "400", description = "Invalid appointment id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{appointmentId}/cancel")
    public ResponseEntity<Void> cancelAppointment(
            @Parameter(example = "3001")
            @PathVariable @Positive(message = "O identificador do agendamento deve ser positivo") Long appointmentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        professionalAppointmentStatusUseCase.cancel(appointmentId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mark appointment as missed", description = "Marks an appointment as missed.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Appointment marked as missed"),
            @ApiResponse(responseCode = "400", description = "Invalid appointment id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{appointmentId}/miss")
    public ResponseEntity<Void> missedAppointment(
            @Parameter(example = "3001")
            @PathVariable @Positive(message = "O identificador do agendamento deve ser positivo") Long appointmentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        professionalAppointmentStatusUseCase.miss(appointmentId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
