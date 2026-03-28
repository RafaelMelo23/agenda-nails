package com.rafael.nailspro.webapp.domain.model;

import com.rafael.nailspro.webapp.domain.enums.appointment.AppointmentStatus;
import com.rafael.nailspro.webapp.infrastructure.dto.appointment.AppointmentCreateDTO;
import com.rafael.nailspro.webapp.infrastructure.exception.BusinessException;
import com.rafael.nailspro.webapp.support.factory.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentTest {

    private static Appointment buildAppointment(AppointmentStatus status) {
        return Appointment.builder()
                .client(TestClientFactory.standard())
                .professional(TestProfessionalFactory.standard())
                .appointmentStatus(status)
                .startDate(Instant.now().minusSeconds(1800))
                .endDate(Instant.now().plusSeconds(1800))
                .salonZoneId(ZoneId.of("America/Sao_Paulo"))
                .addOns(new java.util.ArrayList<>())
                .tenantId("tenant-test")
                .build();
    }

    private static Appointment finishedAppointment(AppointmentStatus status) {
        return Appointment.builder()
                .client(TestClientFactory.standard())
                .professional(TestProfessionalFactory.standard())
                .appointmentStatus(status)
                .startDate(Instant.now().minusSeconds(7200))
                .endDate(Instant.now().minusSeconds(3600))
                .salonZoneId(ZoneId.of("America/Sao_Paulo"))
                .addOns(new java.util.ArrayList<>())
                .tenantId("tenant-test")
                .build();
    }

    @Test
    void miss_whenAppointmentHasFinalStatus_shouldThrowBusinessException() {
        Appointment appointment = buildAppointment(AppointmentStatus.FINISHED);

        assertThrows(BusinessException.class, appointment::miss);
    }

    @Test
    void miss_whenAppointmentNotFinished_shouldThrowBusinessException() {
        Appointment appointment = buildAppointment(AppointmentStatus.CONFIRMED);

        assertThrows(BusinessException.class, appointment::miss);
    }

    @Test
    void miss_whenAppointmentFinished_shouldSetStatusToMissed() {
        Appointment appointment = finishedAppointment(AppointmentStatus.CONFIRMED);

        appointment.miss();

        assertEquals(AppointmentStatus.MISSED, appointment.getAppointmentStatus());
    }

    @Test
    void cancel_whenAppointmentHasFinalStatus_shouldThrowBusinessException() {
        Appointment appointment = buildAppointment(AppointmentStatus.FINISHED);

        assertThrows(BusinessException.class, appointment::cancel);
    }

    @Test
    void cancel_whenAppointmentActive_shouldSetStatusToCancelled() {
        Appointment appointment = buildAppointment(AppointmentStatus.CONFIRMED);

        appointment.cancel();

        assertEquals(AppointmentStatus.CANCELLED, appointment.getAppointmentStatus());
    }

    @Test
    void cancel_whenAppointmentIsWithinTwoDays_shouldIncrementClientCancelledCount() {
        Client client = TestClientFactory.standard();
        int initialCancelled = client.getCanceledAppointments();
        Appointment appointment = Appointment.builder()
                .client(client)
                .appointmentStatus(AppointmentStatus.CONFIRMED)
                .startDate(Instant.now().plus(1, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(1, ChronoUnit.DAYS).plusSeconds(3600))
                .build();

        appointment.cancel();

        assertEquals(initialCancelled + 1, client.getCanceledAppointments());
    }

    @Test
    void cancel_whenAppointmentIsBeyondTwoDays_shouldNotIncrementClientCancelledCount() {
        Client client = TestClientFactory.standard();
        int initialCancelled = client.getCanceledAppointments();
        Appointment appointment = Appointment.builder()
                .client(client)
                .appointmentStatus(AppointmentStatus.CONFIRMED)
                .startDate(Instant.now().plus(3, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(3, ChronoUnit.DAYS).plusSeconds(3600))
                .build();

        appointment.cancel();

        assertEquals(initialCancelled, client.getCanceledAppointments());
    }

    @Test
    void finish_whenAppointmentHasFinalStatus_shouldThrowBusinessException() {
        Appointment appointment = buildAppointment(AppointmentStatus.FINISHED);

        assertThrows(BusinessException.class, appointment::finish);
    }

    @Test
    void finish_whenAppointmentNotFinished_shouldThrowBusinessException() {
        Appointment appointment = buildAppointment(AppointmentStatus.CONFIRMED);

        assertThrows(BusinessException.class, appointment::finish);
    }

    @Test
    void finish_whenAppointmentFinished_shouldSetStatusToFinished() {
        Appointment appointment = finishedAppointment(AppointmentStatus.CONFIRMED);

        appointment.finish();

        assertEquals(AppointmentStatus.FINISHED, appointment.getAppointmentStatus());
    }

    @Test
    void confirm_whenAppointmentNotConfirmed_shouldSetStatusToConfirmed() {
        Appointment appointment = buildAppointment(AppointmentStatus.PENDING);

        appointment.confirm();

        assertEquals(AppointmentStatus.CONFIRMED, appointment.getAppointmentStatus());
    }

    @Test
    void confirm_whenAlreadyConfirmed_shouldDoNothing() {
        Appointment appointment = buildAppointment(AppointmentStatus.CONFIRMED);

        appointment.confirm();

        assertEquals(AppointmentStatus.CONFIRMED, appointment.getAppointmentStatus());
    }

    @Test
    void calculateTotalValue_calculateTotalValue_shouldReturnCorrectValue() {
        SalonService salonService = TestSalonServiceFactory.standard(); // value: 50
        SalonService addOn = TestSalonServiceFactory.addOnWithoutMaintenanceInterval(); // value: 15
        AppointmentAddOn appointmentAddOn = TestAppointmentAddOnFactory.standard(addOn);

        Appointment appointment = Appointment.builder()
                .addOns(List.of(appointmentAddOn))
                .mainSalonService(salonService)
                .build();

        BigDecimal totalValue = appointment.calculateTotalValue();

        assertEquals(new BigDecimal(65), totalValue);
    }

    @Test
    void calculateTotalValue_calculateWithNullService_shouldThrowIllegalStateException() {
        Appointment appointment = Appointment.builder()
                .addOns(List.of())
                .mainSalonService(null)
                .build();

        assertThrows(IllegalStateException.class, appointment::calculateTotalValue);
    }

    @Test
    void create_whenSuccessful_shouldReturnPendingAppointment() {
        AppointmentCreateDTO dto = AppointmentCreateDTO.builder()
                .observation(Optional.of("Testing"))
                .zonedAppointmentDateTime(ZonedDateTime.now())
                .build();
        Client client = TestClientFactory.standard();
        Professional professional = TestProfessionalFactory.standard();
        SalonService mainService = TestSalonServiceFactory.standard();
        SalonProfile salonProfile = TestSalonProfileFactory.standard();
        TimeInterval interval = TimeInterval.builder()
                .realTimeStart(Instant.now())
                .realTimeEnd(Instant.now().plusSeconds(3600))
                .endTimeWithBuffer(Instant.now().plusSeconds(4500))
                .salonZoneId(ZoneId.of("America/Sao_Paulo"))
                .build();

        Appointment appointment = Appointment.create(dto, client, professional, mainService, List.of(), salonProfile, interval);

        assertNotNull(appointment);
        assertEquals(AppointmentStatus.PENDING, appointment.getAppointmentStatus());
        assertEquals(client, appointment.getClient());
        assertEquals(professional, appointment.getProfessional());
        assertEquals(mainService, appointment.getMainSalonService());
        assertEquals("Testing", appointment.getObservations());
        assertEquals(salonProfile.getTradeName(), appointment.getSalonTradeName());
    }

    @Test
    void create_whenSalonProfileHasAutoConfirmation_shouldReturnConfirmedAppointment() {
        AppointmentCreateDTO dto = AppointmentCreateDTO.builder()
                .observation(Optional.empty())
                .zonedAppointmentDateTime(ZonedDateTime.now())
                .build();
        Client client = TestClientFactory.standard();
        Professional professional = TestProfessionalFactory.standard();
        SalonService mainService = TestSalonServiceFactory.standard();
        SalonProfile salonProfile = TestSalonProfileFactory.standard();
        salonProfile.setAutoConfirmationAppointment(true);

        TimeInterval interval = TimeInterval.builder()
                .realTimeStart(Instant.now())
                .realTimeEnd(Instant.now().plusSeconds(3600))
                .endTimeWithBuffer(Instant.now().plusSeconds(4500))
                .salonZoneId(ZoneId.of("America/Sao_Paulo"))
                .build();

        Appointment appointment = Appointment.create(dto, client, professional, mainService, List.of(), salonProfile, interval);

        assertEquals(AppointmentStatus.CONFIRMED, appointment.getAppointmentStatus());
    }

    @Test
    void create_whenArgumentsAreNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                Appointment.create(null, null, null, null, null, null, null));
    }

    @Test
    void calculateIntervalAndBuffer_shouldCalculateCorrectly() {
        ZonedDateTime startTime = ZonedDateTime.of(2023, 10, 10, 10, 0, 0, 0, ZoneId.of("America/Sao_Paulo"));
        AppointmentCreateDTO dto = AppointmentCreateDTO.builder()
                .zonedAppointmentDateTime(startTime)
                .build();
        long duration = 3600; // 1 hour
        SalonProfile profile = TestSalonProfileFactory.withCustomBuffer(15);

        TimeInterval result = Appointment.calculateIntervalAndBuffer(dto, duration, profile);

        assertEquals(startTime.toInstant(), result.realTimeStart());
        assertEquals(startTime.toInstant().plusSeconds(3600), result.realTimeEnd());
        assertEquals(startTime.toInstant().plusSeconds(3600 + 900), result.endTimeWithBuffer());
        assertEquals(profile.getZoneId(), result.salonZoneId());
    }

    @Test
    void calculateDurationInSeconds_WithMultipleAddOns_ReturnsCorrectTotalDuration() {
        SalonService main = TestSalonServiceFactory.withDuration(3600); // 1 hour
        List<AppointmentAddOn> addOns = List.of(
                TestAppointmentAddOnFactory.standard(TestSalonServiceFactory.withDuration(600)), // 10 mins
                TestAppointmentAddOnFactory.standard(TestSalonServiceFactory.withDuration(300))  // 5 mins
        );

        long totalDuration = Appointment.calculateDurationInSeconds(main, addOns);

        assertEquals(4500, totalDuration);
    }

    @Test
    void calculateDurationInSeconds_WithNullMainService_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                Appointment.calculateDurationInSeconds(null, List.of()));
    }

    @Test
    void calculateDurationInSeconds_WithNullAddOnsList_ReturnsOnlyMainServiceDuration() {
        SalonService main = TestSalonServiceFactory.withDuration(1800);

        long totalDuration = Appointment.calculateDurationInSeconds(main, null);

        assertEquals(1800, totalDuration);
    }

    @Test
    void calculateDurationInSeconds_WithEmptyAddOns_ReturnsOnlyMainServiceDuration() {
        SalonService main = TestSalonServiceFactory.withDuration(1800);

        long totalDuration = Appointment.calculateDurationInSeconds(main, List.of());

        assertEquals(1800, totalDuration);
    }

    @Test
    void calculateDurationInSeconds_WithNullEntriesInList_FiltersThemOut() {
        SalonService main = TestSalonServiceFactory.withDuration(3600);

        AppointmentAddOn validAddon = TestAppointmentAddOnFactory.standard(TestSalonServiceFactory.withDuration(600));
        AppointmentAddOn corruptedAddon = new AppointmentAddOn();

        List<AppointmentAddOn> addOns = new java.util.ArrayList<>();
        addOns.add(validAddon);
        addOns.add(null);
        addOns.add(corruptedAddon);

        long totalDuration = Appointment.calculateDurationInSeconds(main, addOns);

        assertEquals(4200, totalDuration);
    }

    @Test
    void ensureAppointmentIsActive_whenEnded_shouldThrowBusinessException() {
        Appointment appointment = finishedAppointment(AppointmentStatus.CONFIRMED);
        assertThrows(BusinessException.class, appointment::ensureAppointmentIsActive);
    }

    @Test
    void ensureStatusIsNotFinal_whenFinalStatus_shouldThrowBusinessException() {
        Appointment finished = buildAppointment(AppointmentStatus.FINISHED);
        Appointment missed = buildAppointment(AppointmentStatus.MISSED);
        Appointment cancelled = buildAppointment(AppointmentStatus.CANCELLED);

        assertThrows(BusinessException.class, finished::ensureStatusIsNotFinal);
        assertThrows(BusinessException.class, missed::ensureStatusIsNotFinal);
        assertThrows(BusinessException.class, cancelled::ensureStatusIsNotFinal);
    }
}
