package com.rafael.agendanails.webapp.infrastructure.helper;

import com.rafael.agendanails.webapp.application.salon.business.SalonProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class DateAndZoneHelper {

    private final SalonProfileService salonProfileService;

    public ZonedDateTime toZonedDateTime(Instant instant) {
        ZoneId salonZoneId = salonProfileService.getSalonZoneIdByContext();

        return ZonedDateTime.ofInstant(instant, salonZoneId);
    }

    public ZonedDateTime toZonedDateTime(Instant instant, String tenantId) {
        ZoneId salonZoneId = salonProfileService.getSalonZoneId(tenantId);

        return ZonedDateTime.ofInstant(instant, salonZoneId);
    }
}
