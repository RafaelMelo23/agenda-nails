package com.rafael.agendanails.webapp.application.appointment.booking;

import com.rafael.agendanails.webapp.application.appointment.BookingPolicyService;
import com.rafael.agendanails.webapp.application.professional.ProfessionalWorkScheduleUseCase;
import com.rafael.agendanails.webapp.application.salon.business.SalonProfileService;
import com.rafael.agendanails.webapp.application.salon.business.SalonServiceService;
import com.rafael.agendanails.webapp.domain.AvailabilityDomainService;
import com.rafael.agendanails.webapp.domain.enums.user.UserRole;
import com.rafael.agendanails.webapp.domain.model.*;
import com.rafael.agendanails.webapp.domain.repository.AppointmentRepository;
import com.rafael.agendanails.webapp.domain.repository.ClientRepository;
import com.rafael.agendanails.webapp.domain.repository.ProfessionalRepository;
import com.rafael.agendanails.webapp.infrastructure.dto.appointment.AppointmentCreateDTO;
import com.rafael.agendanails.webapp.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingAppointmentUseCase {

    private final ClientRepository clientRepository;
    private final AppointmentRepository repository;
    private final ProfessionalWorkScheduleUseCase professionalWorkScheduleUseCase;
    private final AvailabilityDomainService availabilityDomainService;
    private final SalonProfileService salonProfileService;
    private final BookingPolicyService bookingPolicyManager;
    private final ProfessionalRepository professionalRepository;
    private final SalonServiceService salonService;

    @Transactional
    public void bookAppointment(
            AppointmentCreateDTO dto,
            UserPrincipal principal
    ) {
        UUID professionalId = UUID.fromString(dto.professionalExternalId());

        Professional professional =
                professionalRepository.findByExternalIdWithPessimisticLock(professionalId);

        SalonService mainService =
                salonService.findById(dto.mainServiceId());

        if (!mainService.getProfessionals().contains(professional)) {
            throw new BusinessException("O profissional selecionado não realiza este serviço.");
        }

        Set<SalonService> addOnServices =
                salonService.findAddOnsByIds(dto.addOnsIds());

        List<AppointmentAddOn> addOns = getAppointmentAddOns(addOnServices, professional);

        Client client = clientRepository.findById(principal.getUserId())
                .orElseThrow(() -> new BusinessException("Cliente não encontrado"));

        if (!client.getUserRole().equals(UserRole.CLIENT)) {
            throw new BusinessException("Apenas clientes podem agendar");
        }

        SalonProfile salonProfile =
                salonProfileService.getByTenantId(principal.getTenantId());

        TimeInterval interval =
                Appointment.calculateIntervalAndBuffer(
                        dto,
                        Appointment.calculateDurationInSeconds(mainService, addOns),
                        salonProfile);

        professionalWorkScheduleUseCase.checkProfessionalAvailability(professionalId, interval);

        availabilityDomainService.checkIfProfessionalHasTimeConflicts(professionalId, interval);

        bookingPolicyManager.enforceBookingHorizon(
                dto.zonedAppointmentDateTime().toLocalDateTime(),
                principal
        );

        Appointment appointment =
                Appointment.create(
                        dto,
                        client,
                        professional,
                        mainService,
                        addOns,
                        salonProfile,
                        interval
                );

        repository.save(appointment);
        repository.flush();

        if (salonProfile.isAutoConfirmationAppointment()) {
            appointment.confirm();
            repository.save(appointment);
        }
    }

    private static List<AppointmentAddOn> getAppointmentAddOns(Set<SalonService> addOnServices, Professional professional) {
        return addOnServices.stream()
                .map(s -> {
                    if (!s.getProfessionals().contains(professional)) {
                        throw new BusinessException("O profissional selecionado não realiza o adicional: " + s.getName());
                    }
                    return AppointmentAddOn.builder()
                            .service(s)
                            .quantity(1)
                            .unitPriceSnapshot(s.getValue())
                            .build();
                })
                .collect(Collectors.toList());
    }
}