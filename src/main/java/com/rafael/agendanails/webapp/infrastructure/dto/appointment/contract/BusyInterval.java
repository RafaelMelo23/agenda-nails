package com.rafael.agendanails.webapp.infrastructure.dto.appointment.contract;

import java.time.LocalDate;
import java.time.LocalTime;

public interface BusyInterval {

    LocalTime getStart();
    LocalTime getEnd();
    LocalDate getDate();
}
