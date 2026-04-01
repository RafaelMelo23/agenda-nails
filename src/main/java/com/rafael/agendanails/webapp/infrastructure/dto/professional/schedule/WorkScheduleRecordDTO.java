package com.rafael.agendanails.webapp.infrastructure.dto.professional.schedule;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Builder
public record WorkScheduleRecordDTO(
        Long id,

        @NotNull(message = "O dia da semana é obrigatório")
        DayOfWeek dayOfWeek,

        @NotNull(message = "O horário de início é obrigatório")
        LocalTime startTime,

        @NotNull(message = "O horário de término é obrigatório")
        LocalTime endTime,

        @NotNull(message = "O início do intervalo é obrigatório")
        LocalTime lunchBreakStartTime,

        @NotNull(message = "O fim do intervalo é obrigatório")
        LocalTime lunchBreakEndTime,

        Boolean isActive
) {}