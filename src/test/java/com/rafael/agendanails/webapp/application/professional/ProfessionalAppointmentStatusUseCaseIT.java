package com.rafael.agendanails.webapp.application.professional;

import com.rafael.agendanails.webapp.domain.enums.appointment.AppointmentStatus;
import com.rafael.agendanails.webapp.domain.model.Appointment;
import com.rafael.agendanails.webapp.domain.model.Client;
import com.rafael.agendanails.webapp.domain.model.Professional;
import com.rafael.agendanails.webapp.domain.model.SalonService;
import com.rafael.agendanails.webapp.infrastructure.dto.appointment.booking.event.AppointmentCancelledEvent;
import com.rafael.agendanails.webapp.infrastructure.dto.appointment.booking.event.AppointmentFinishedEvent;
import com.rafael.agendanails.webapp.infrastructure.dto.appointment.booking.event.AppointmentMissedEvent;
import com.rafael.agendanails.webapp.infrastructure.exception.BusinessException;
import com.rafael.agendanails.webapp.shared.tenant.TenantContext;
import com.rafael.agendanails.webapp.support.BaseIntegrationTest;
import com.rafael.agendanails.webapp.support.factory.TestAppointmentFactory;
import com.rafael.agendanails.webapp.support.factory.TestClientFactory;
import com.rafael.agendanails.webapp.support.factory.TestProfessionalFactory;
import com.rafael.agendanails.webapp.support.factory.TestSalonServiceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RecordApplicationEvents
class ProfessionalAppointmentStatusUseCaseIT extends BaseIntegrationTest {

    @Autowired
    private ProfessionalAppointmentStatusUseCase useCase;

    @Autowired
    private ApplicationEvents events;

    @Test
    void shouldConfirmAppointment() {
        Professional pro = professionalRepository.save(TestProfessionalFactory.builder().build());
        Client client = clientRepository.save(TestClientFactory.builder().build());
        SalonService service = salonServiceRepository.save(TestSalonServiceFactory.builder().build());
        Appointment appointment = appointmentRepository.save(TestAppointmentFactory.standardForIt(client, pro, service));

        useCase.confirm(appointment.getId(), pro.getId());

        Appointment updated = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertThat(updated.getAppointmentStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    void shouldFinishAppointmentAndPublishEvent() {
        Professional pro = professionalRepository.save(TestProfessionalFactory.builder().build());
        Client client = clientRepository.save(TestClientFactory.builder().build());
        SalonService service = salonServiceRepository.save(TestSalonServiceFactory.builder().build());
        Appointment appointment = appointmentRepository.save(TestAppointmentFactory.pastForIt(client, pro, service, AppointmentStatus.CONFIRMED));

        useCase.finish(appointment.getId(), pro.getId());

        Appointment updated = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertThat(updated.getAppointmentStatus()).isEqualTo(AppointmentStatus.FINISHED);
        assertThat(events.stream(AppointmentFinishedEvent.class)).hasSize(1);
    }

    @Test
    void shouldCancelAppointmentAndPublishEvent() {
        Professional pro = professionalRepository.save(TestProfessionalFactory.builder().build());
        Client client = clientRepository.save(TestClientFactory.builder().build());
        SalonService service = salonServiceRepository.save(TestSalonServiceFactory.builder().build());
        Appointment appointment = appointmentRepository.save(TestAppointmentFactory.futureForIt(client, pro, service, AppointmentStatus.CONFIRMED));

        useCase.cancel(appointment.getId(), pro.getId());

        Appointment updated = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertThat(updated.getAppointmentStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(events.stream(AppointmentCancelledEvent.class)).hasSize(1);
    }

    @Test
    void shouldMissAppointmentAndPublishEvent() {
        Professional pro = professionalRepository.save(TestProfessionalFactory.builder().build());
        Client client = clientRepository.save(TestClientFactory.builder().build());
        SalonService service = salonServiceRepository.save(TestSalonServiceFactory.builder().build());
        Appointment appointment = appointmentRepository.save(TestAppointmentFactory.pastForIt(client, pro, service, AppointmentStatus.CONFIRMED));

        useCase.miss(appointment.getId(), pro.getId());

        Appointment updated = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertThat(updated.getAppointmentStatus()).isEqualTo(AppointmentStatus.MISSED);
        assertThat(events.stream(AppointmentMissedEvent.class)).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenAppointmentNotBelongsToProfessional() {
        Professional pro1 = professionalRepository.save(TestProfessionalFactory.builder().build());
        Professional pro2 = professionalRepository.save(TestProfessionalFactory.builder().build());
        Client client = clientRepository.save(TestClientFactory.builder().build());
        SalonService service = salonServiceRepository.save(TestSalonServiceFactory.builder().build());
        Appointment appointment = appointmentRepository.save(TestAppointmentFactory.standardForIt(client, pro1, service));

        assertThatThrownBy(() -> useCase.confirm(appointment.getId(), pro2.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Agendamento não encontrado ou não pertence ao profissional");
    }

    @Test
    void shouldRespectTenantIsolation() {
        String tenantA = "tenant-a";
        String tenantB = "tenant-b";

        TenantContext.setTenant(tenantA);
        Professional proA = professionalRepository.save(TestProfessionalFactory.builder().tenantId(tenantA).build());
        Client clientA = clientRepository.save(TestClientFactory.builder().tenantId(tenantA).build());
        SalonService serviceA = salonServiceRepository.save(TestSalonServiceFactory.builder().tenantId(tenantA).build());
        Appointment appointmentA = appointmentRepository.save(TestAppointmentFactory.standardForIt(clientA, proA, serviceA));

        TenantContext.setTenant(tenantB);
        
        assertThatThrownBy(() -> useCase.confirm(appointmentA.getId(), proA.getId()))
                .isInstanceOf(BusinessException.class);
    }
}
