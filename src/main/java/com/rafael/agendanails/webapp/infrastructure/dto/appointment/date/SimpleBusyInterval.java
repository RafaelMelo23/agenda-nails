package com.rafael.agendanails.webapp.infrastructure.dto.appointment.date;

import com.rafael.agendanails.webapp.infrastructure.dto.appointment.contract.BusyInterval;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record SimpleBusyInterval(LocalTime start,
                                 LocalTime end,
                                 LocalDate date) implements BusyInterval {

    @Override
    public LocalTime getStart() {
        return start();
    }

    @Override
    public LocalTime getEnd() {
        return end();
    }

    @Override
    public LocalDate getDate() {
        return date();
    }
}
