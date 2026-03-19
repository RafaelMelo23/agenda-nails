package com.rafael.nailspro.webapp.support.factory;

import com.rafael.nailspro.webapp.domain.model.WorkSchedule;
import com.rafael.nailspro.webapp.domain.model.Professional;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class TestWorkScheduleFactory {

    public static WorkSchedule standard(DayOfWeek dayOfWeek, Professional professional) {
        WorkSchedule builtWs = WorkSchedule.builder()
                .dayOfWeek(dayOfWeek)
                .workStart(LocalTime.of(9, 0))
                .workEnd(LocalTime.of(18, 0))
                .lunchBreakStartTime(LocalTime.of(12, 0))
                .lunchBreakEndTime(LocalTime.of(13, 0))
                .isActive(true)
                .professional(professional)
                .build();

        builtWs.setId(1L);
        builtWs.setTenantId("tenant-test");
        return builtWs;
    }

    public static WorkSchedule withoutLunch(DayOfWeek dayOfWeek, Professional professional) {
        WorkSchedule builtWs = WorkSchedule.builder()
                .dayOfWeek(dayOfWeek)
                .workStart(LocalTime.of(8, 0))
                .workEnd(LocalTime.of(14, 0))
                .lunchBreakStartTime(null)
                .lunchBreakEndTime(null)
                .isActive(true)
                .professional(professional)
                .build();

        builtWs.setId(2L);
        builtWs.setTenantId("tenant-test");
        return builtWs;
    }

    public static WorkSchedule custom(DayOfWeek dayOfWeek,
                                      LocalTime start,
                                      LocalTime end,
                                      LocalTime lunchStart,
                                      LocalTime lunchEnd,
                                      Professional professional) {
        WorkSchedule builtWs = WorkSchedule.builder()
                .dayOfWeek(dayOfWeek)
                .workStart(start)
                .workEnd(end)
                .lunchBreakStartTime(lunchStart)
                .lunchBreakEndTime(lunchEnd)
                .isActive(true)
                .professional(professional)
                .build();

        builtWs.setId(3L);
        builtWs.setTenantId("tenant-test");
        return builtWs;
    }
}
