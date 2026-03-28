package com.rafael.nailspro.webapp.domain;

import com.rafael.nailspro.webapp.domain.model.*;
import com.rafael.nailspro.webapp.infrastructure.dto.appointment.booking.AppointmentTimeWindow;
import com.rafael.nailspro.webapp.infrastructure.exception.BusinessException;
import com.rafael.nailspro.webapp.support.factory.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class BookingPolicyTest {

    @InjectMocks
    private BookingPolicy bookingPolicy;

    @Mock
    private Clock clock;

    private static final Instant FIXED_INSTANT = Instant.parse("2026-03-28T10:00:00Z");
    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate TODAY = LocalDate.ofInstant(FIXED_INSTANT, ZONE_ID);

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(FIXED_INSTANT);
        lenient().when(clock.getZone()).thenReturn(ZONE_ID);
    }

    @Test
    void resolveAllowedWindowDays_returnsStandardDefault() {
        int result = bookingPolicy.resolveAllowedWindowDays(false, false, null, null);
        assertEquals(7, result);
    }

    @Test
    void resolveAllowedWindowDays_handlesNullBooleans() {
        int result = bookingPolicy.resolveAllowedWindowDays(null, null, 30, 10);
        assertEquals(10, result);
    }

    @Test
    void resolveAllowedWindowDays_returnsStandardWhenPrioritizeIsTrueButNotLoyal() {
        int result = bookingPolicy.resolveAllowedWindowDays(true, null, 30, 10);
        assertEquals(10, result);
    }

    @Test
    void resolveAllowedWindowDays_returnsLoyalDefault() {
        int result = bookingPolicy.resolveAllowedWindowDays(true, true, null, null);
        assertEquals(30, result);
    }

    @Test
    void validateBookingHorizon_doesNotThrowWhenInsideWindow() {
        LocalDate requestedDate = TODAY.plusDays(3);

        assertDoesNotThrow(() -> bookingPolicy.validateBookingHorizon(requestedDate, 7, TODAY));
    }

    @Test
    void validateBookingHorizon_throwsExceptionWhenOutsideWindow() {
        LocalDate requestedDate = TODAY.plusDays(8);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingPolicy.validateBookingHorizon(requestedDate, 7, TODAY));

        assertEquals("Sua janela de agendamento não é preferencial, aguarde alguns dias para reservar esta data.", exception.getMessage());
    }

    @Test
    void buildWindow_createsCorrectWindow() {
        LocalDate start = TODAY;
        AppointmentTimeWindow window = bookingPolicy.buildWindow(start, 7);

        assertEquals(start, window.start());
        assertEquals(start.plusDays(7), window.end());
    }

    @Test
    void determineStartDate_returnsToday_WhenMaintenanceIntervalIsNull() {
        Client client = TestClientFactory.standard();
        SalonService salonService = TestSalonServiceFactory.standardWithoutMaintenanceInterval();

        Appointment appointment = TestAppointmentFactory.standard(
                client,
                TestProfessionalFactory.standard(),
                salonService
        );

        LocalDate startDate = bookingPolicy.determineStartDate(List.of(salonService), appointment);

        assertEquals(TODAY, startDate);
    }

    @Test
    void determineStartDate_returnsToday_WhenMaintenanceIntervalAndLastAppointmentAreNull() {
        SalonService salonService = TestSalonServiceFactory.standardWithoutMaintenanceInterval();

        LocalDate bookingPolicyStartDate = bookingPolicy.determineStartDate(List.of(salonService), null);

        assertEquals(TODAY, bookingPolicyStartDate);
    }

    @Test
    void determineStartDate_returnsTodayWhenLastAppointmentIsEmpty() {
        SalonService salonService = TestSalonServiceFactory.standardWithoutMaintenanceInterval();

        LocalDate startDate = bookingPolicy.determineStartDate(List.of(salonService), null);

        assertEquals(TODAY, startDate);
    }

    @Test
    void determineStartDate_calculatesCorrectDateWithMaintenanceInterval() {
        Client client = TestClientFactory.standard();
        SalonService salonService = TestSalonServiceFactory.withMaintenanceInterval(20);

        Appointment appointment = TestAppointmentFactory.standard(
                client,
                TestProfessionalFactory.standard(),
                salonService
        );
        LocalDate bookingPolicyStartDate = bookingPolicy.determineStartDate(List.of(salonService), appointment);

        LocalDate appointmentDate = LocalDate.ofInstant(appointment.getEndDate(), appointment.getSalonZoneId());
        LocalDate expectedDate = appointmentDate.plusDays(salonService.getMaintenanceIntervalDays() - 3);

        assertEquals(expectedDate, bookingPolicyStartDate);
    }

    @Test
    void calculateEarliestRecommendedDate_returnsNowIfAppointmentNull() {
        Instant recommendedDate = bookingPolicy.calculateEarliestRecommendedDate(null);
        assertEquals(FIXED_INSTANT, recommendedDate);
    }

    @Test
    void calculateEarliestRecommendedDate_returnsMainServiceMaintenanceInterval() {
        Client client = TestClientFactory.standard();
        Professional professional = TestProfessionalFactory.standard();
        SalonService salonService = TestSalonServiceFactory.withMaintenanceInterval(20);
        AppointmentAddOn addOn = TestAppointmentAddOnFactory.standard(TestSalonServiceFactory.addOnWithMaintenanceInterval(15));

        Appointment appointment = TestAppointmentFactory.withAddOns(
                client,
                professional,
                salonService,
                List.of(addOn)
        );

        Instant recommendedDate = bookingPolicy.calculateEarliestRecommendedDate(appointment);
        Instant expectedDate = appointment.getEndDate().plus(salonService.getMaintenanceIntervalDays(), ChronoUnit.DAYS);

        assertEquals(expectedDate, recommendedDate);
    }

    @Test
    void calculateEarliestRecommendedDate_returnsAddOnMaintenanceInterval() {
        Client client = TestClientFactory.standard();
        Professional professional = TestProfessionalFactory.standard();
        SalonService salonService = TestSalonServiceFactory.withMaintenanceInterval(20);
        AppointmentAddOn addOn = TestAppointmentAddOnFactory.standard(TestSalonServiceFactory.addOnWithMaintenanceInterval(25));

        Appointment appointment = TestAppointmentFactory.withAddOns(
                client,
                professional,
                salonService,
                List.of(addOn)
        );

        Instant recommendedDate = bookingPolicy.calculateEarliestRecommendedDate(appointment);
        Instant expectedDate = appointment.getEndDate().plus(addOn.getService().getMaintenanceIntervalDays(), ChronoUnit.DAYS);

        assertEquals(expectedDate, recommendedDate);
    }

    @Test
    void calculateEarliestRecommendedDate_returnsNowIfMaintenanceIntervalIsNull() {
        Client client = TestClientFactory.standard();
        Professional professional = TestProfessionalFactory.standard();
        SalonService salonService = TestSalonServiceFactory.standardWithoutMaintenanceInterval();
        AppointmentAddOn addOn = TestAppointmentAddOnFactory.standard(TestSalonServiceFactory.addOnWithoutMaintenanceInterval());

        Appointment appointment = TestAppointmentFactory.withAddOns(
                client,
                professional,
                salonService,
                List.of(addOn)
        );

        Instant recommendedDate = bookingPolicy.calculateEarliestRecommendedDate(appointment);

        assertEquals(FIXED_INSTANT, recommendedDate);
    }
}
