package com.rafael.agendanails.webapp.infrastructure.dto.appointment;

import com.rafael.agendanails.webapp.domain.enums.appointment.AppointmentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Builder
public record ProfessionalAppointmentScheduleDTO(Long appointmentId,

                                                 Long clientId,
                                                 String clientName,
                                                 String clientPhoneNumber,
                                                 Integer clientMissedAppointments,
                                                 Integer clientCanceledAppointments,

                                                 AppointmentStatus status,
                                                 BigDecimal totalValue,
                                                 String observations,

                                                 ZonedDateTime startDate,
                                                 ZonedDateTime endDate) {
}
