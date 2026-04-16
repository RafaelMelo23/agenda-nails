package com.rafael.agendanails.webapp.application.appointment.booking;

import com.rafael.agendanails.webapp.application.professional.ProfessionalWorkScheduleUseCase;
import com.rafael.agendanails.webapp.domain.AvailabilityDomainService;
import com.rafael.agendanails.webapp.domain.model.UserPrincipal;
import com.rafael.agendanails.webapp.infrastructure.dto.appointment.AppointmentCreateDTO;
import com.rafael.agendanails.webapp.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingValidator {

    private final ProfessionalWorkScheduleUseCase professionalWorkScheduleUseCase;
    private final AvailabilityDomainService availabilityDomainService;
    private final BookingPolicyService bookingPolicyManager;

    public void validate(BookingContext context, AppointmentCreateDTO dto, UserPrincipal principal) {
        validateIfUserNotProfessional(principal);

        professionalWorkScheduleUseCase.checkProfessionalAvailability(
                context.professional().getExternalId(),
                context.interval());

        availabilityDomainService.checkIfProfessionalHasTimeConflicts(
                context.professional().getExternalId(),
                context.interval());

        bookingPolicyManager.enforceBookingHorizon(
                dto.zonedAppointmentDateTime().toLocalDateTime(),
                principal
        );
    }

    private static void validateIfUserNotProfessional(UserPrincipal principal) {
        if (principal.isProfessional()) {
            throw new BusinessException("Profissionais não podem agendar horários.");
        }
    }
}