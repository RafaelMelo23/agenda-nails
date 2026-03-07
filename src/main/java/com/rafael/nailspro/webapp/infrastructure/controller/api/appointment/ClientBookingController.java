package com.rafael.nailspro.webapp.infrastructure.controller.api.appointment;

import com.rafael.nailspro.webapp.application.appointment.booking.BookingAppointmentUseCase;
import com.rafael.nailspro.webapp.application.appointment.booking.CancelAppointmentUseCase;
import com.rafael.nailspro.webapp.application.appointment.booking.FindProfessionalAvailabilityUseCase;
import com.rafael.nailspro.webapp.domain.model.UserPrincipal;
import com.rafael.nailspro.webapp.infrastructure.dto.appointment.AppointmentCreateDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.appointment.ProfessionalAvailabilityDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.professional.FindProfessionalAvailabilityDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/booking")
public class ClientBookingController {

    private final BookingAppointmentUseCase bookingAppointmentUseCase;
    private final CancelAppointmentUseCase cancelAppointmentUseCase;
    private final FindProfessionalAvailabilityUseCase findProfessionalAvailabilityUseCase;

    @PostMapping
    public ResponseEntity<Void> bookAppointment(@Valid @RequestBody AppointmentCreateDTO appointmentDTO,
                                                @AuthenticationPrincipal UserPrincipal userPrincipal) {

        bookingAppointmentUseCase.bookAppointment(appointmentDTO, userPrincipal);
        return ResponseEntity.status(201).build();
    }

    @PatchMapping("/{appointmentId}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long appointmentId,
                                                  @AuthenticationPrincipal UserPrincipal userPrincipal) {

        cancelAppointmentUseCase.cancelAppointment(appointmentId, userPrincipal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{professionalExternalId}/availability")
    public ResponseEntity<ProfessionalAvailabilityDTO> findAvailableProfessionalTimes(
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @PathVariable
            @NotBlank(message = "O identificador externo do profissional é obrigatório.")
            String professionalExternalId,

            @RequestParam
            @Min(value = 1, message = "A duração do serviço deve ser maior que zero segundos.")
            int serviceDurationInSeconds,

            @RequestParam
            @NotEmpty(message = "Pelo menos um serviço deve ser informado.")
            List<@NotNull(message = "O ID do serviço não pode ser nulo.") Long> servicesIds
    ) {

        FindProfessionalAvailabilityDTO dto = FindProfessionalAvailabilityDTO.builder()
                .professionalExternalId(professionalExternalId)
                .serviceDurationInSeconds(serviceDurationInSeconds)
                .servicesIds(servicesIds)
                .build();

        return ResponseEntity.ok(findProfessionalAvailabilityUseCase.findAvailableTimes(dto, userPrincipal));
    }
}