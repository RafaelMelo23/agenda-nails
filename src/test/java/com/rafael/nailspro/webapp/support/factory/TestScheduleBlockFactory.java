package com.rafael.nailspro.webapp.support.factory;

import com.rafael.nailspro.webapp.domain.model.ScheduleBlock;
import com.rafael.nailspro.webapp.domain.model.Professional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TestScheduleBlockFactory {

    public static ScheduleBlock atSpecificTime(Instant start, Instant end, Professional professional) {
        return ScheduleBlock.builder()
                .id(1L)
                .dateStartTime(start)
                .dateEndTime(end)
                .isWholeDayBlocked(false)
                .reason("Test Block")
                .professional(professional)
                .tenantId("tenant-test")
                .build();
    }

    public static ScheduleBlock wholeDay(Instant startOfDay, Professional professional) {
        return ScheduleBlock.builder()
                .id(2L)
                .dateStartTime(startOfDay)
                .dateEndTime(startOfDay.plus(24, ChronoUnit.HOURS))
                .isWholeDayBlocked(true)
                .reason("Day Off")
                .professional(professional)
                .tenantId("tenant-test")
                .build();
    }
}
