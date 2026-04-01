package com.rafael.agendanails.webapp.infrastructure.mapper;

import com.rafael.agendanails.webapp.domain.model.Appointment;
import com.rafael.agendanails.webapp.domain.model.Professional;
import com.rafael.agendanails.webapp.infrastructure.dto.appointment.ProfessionalAppointmentScheduleDTO;
import com.rafael.agendanails.webapp.infrastructure.dto.professional.ProfessionalResponseDTO;
import com.rafael.agendanails.webapp.infrastructure.dto.professional.ProfessionalSimplifiedDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProfessionalMapper {

    public Set<ProfessionalSimplifiedDTO> mapProfessionalsToSimplifiedDTO(Set<Professional> professionals) {
        return professionals.stream()
                .map(p -> ProfessionalSimplifiedDTO.builder()
                        .externalId(p.getExternalId())
                        .name(p.getFullName())
                        .professionalPicture(p.getProfessionalPicture())
                        .build())
                .collect(Collectors.toSet());
    }

    public static ProfessionalAppointmentScheduleDTO toScheduleDTO(Appointment ap) {
        return ProfessionalAppointmentScheduleDTO.builder()
                .appointmentId(ap.getId())
                .clientId(ap.getClient().getId())
                .clientName(ap.getClient().getFullName())
                .clientPhoneNumber(ap.getClient().getPhoneNumber())
                .clientMissedAppointments(ap.getClient().getMissedAppointments())
                .clientCanceledAppointments(ap.getClient().getCanceledAppointments())
                .status(ap.getAppointmentStatus())
                .totalValue(ap.calculateTotalValue())
                .observations(ap.getObservations())
                .startDate(ap.getStartDate().atZone(ap.getSalonZoneId()))
                .endDate(ap.getEndDate().atZone(ap.getSalonZoneId()))
                .build();
    }

    public static ProfessionalResponseDTO toDTO(Professional professional) {
        return ProfessionalResponseDTO.builder()
                .id(professional.getId())
                .externalId(professional.getExternalId())
                .name(professional.getFullName())
                .email(professional.getEmail())
                .professionalPicture(professional.getProfessionalPicture())
                .isActive(professional.getIsActive())
                .isFirstLogin(professional.getIsFirstLogin())
                .build();
    }

    public static List<ProfessionalResponseDTO> toDTOList(List<Professional> professionals) {
        return professionals.stream()
                .map(ProfessionalMapper::toDTO)
                .toList();
    }
}