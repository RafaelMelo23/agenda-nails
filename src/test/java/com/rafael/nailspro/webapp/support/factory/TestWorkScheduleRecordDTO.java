package com.rafael.nailspro.webapp.support.factory;

import com.rafael.nailspro.webapp.infrastructure.dto.professional.schedule.WorkScheduleRecordDTO;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class TestWorkScheduleRecordDTO {

    public static WorkScheduleRecordDTO createMondayStandardShift() {
        return new WorkScheduleRecordDTO(
                1L,
                DayOfWeek.MONDAY,
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                true
        );
    }

    public static WorkScheduleRecordDTO createTuesdayEarlyShift() {
        return new WorkScheduleRecordDTO(
                2L,
                DayOfWeek.TUESDAY,
                LocalTime.of(6, 0),
                LocalTime.of(15, 0),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                true
        );
    }

    public static WorkScheduleRecordDTO createWednesdayLateShift() {
        return new WorkScheduleRecordDTO(
                3L,
                DayOfWeek.WEDNESDAY,
                LocalTime.of(13, 0),
                LocalTime.of(22, 0),
                LocalTime.of(17, 0),
                LocalTime.of(18, 0),
                true
        );
    }

    public static WorkScheduleRecordDTO createThursdayInactivePartTime() {
        return new WorkScheduleRecordDTO(
                4L,
                DayOfWeek.THURSDAY,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                LocalTime.of(10, 0),
                LocalTime.of(10, 15),
                false
        );
    }
}