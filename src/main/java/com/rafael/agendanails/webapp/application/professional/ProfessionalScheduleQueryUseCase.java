package com.rafael.agendanails.webapp.application.professional;

import com.rafael.agendanails.webapp.domain.repository.AppointmentRepository;
import com.rafael.agendanails.webapp.infrastructure.dto.appointment.ProfessionalAppointmentScheduleDTO;
import com.rafael.agendanails.webapp.infrastructure.mapper.ProfessionalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessionalScheduleQueryUseCase {

    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<ProfessionalAppointmentScheduleDTO> findProfessionalAppointmentsByDay(Long professionalId,
                                                                                      ZonedDateTime start,
                                                                                      ZonedDateTime end) {
        return appointmentRepository
                .findByProfessional_IdAndStartDateBetween(professionalId, start.toInstant(), end.toInstant())
                .stream()
                .map(ProfessionalMapper::toScheduleDTO)
                .toList();
    }
}