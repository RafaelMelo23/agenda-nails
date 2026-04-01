package com.rafael.agendanails.webapp.infrastructure.dto.appointment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProfessionalAvailabilityDTO(ZoneId zoneId,
                                          List<AppointmentTimesDTO> appointmentTimesDTOList,
                                          ZonedDateTime earliestRecommendedDate
                                          ) {
}
