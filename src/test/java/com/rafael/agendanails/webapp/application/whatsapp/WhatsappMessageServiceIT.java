package com.rafael.agendanails.webapp.application.whatsapp;

import com.rafael.agendanails.webapp.domain.enums.whatsapp.WhatsappMessageStatus;
import com.rafael.agendanails.webapp.domain.enums.whatsapp.WhatsappMessageType;
import com.rafael.agendanails.webapp.domain.model.*;
import com.rafael.agendanails.webapp.shared.tenant.TenantContext;
import com.rafael.agendanails.webapp.support.BaseIntegrationTest;
import com.rafael.agendanails.webapp.support.factory.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WhatsappMessageServiceIT extends BaseIntegrationTest {

    @Autowired
    private WhatsappMessageService whatsappMessageService;

    @Test
    void shouldPrepareAppointmentMessage() {
        Professional pro = professionalRepository.save(TestProfessionalFactory.builder().build());
        Client client = clientRepository.save(TestClientFactory.builder().build());
        SalonService service = salonServiceRepository.save(TestSalonServiceFactory.builder().build());
        Appointment appointment = appointmentRepository.save(TestAppointmentFactory.standardForIt(client, pro, service));

        WhatsappMessage message = whatsappMessageService.prepareAppointmentMessage(appointment.getId(), WhatsappMessageType.CONFIRMATION);

        assertThat(message.getAppointment().getId()).isEqualTo(appointment.getId());
        assertThat(message.getMessageType()).isEqualTo(WhatsappMessageType.CONFIRMATION);
        assertThat(message.getMessageStatus()).isEqualTo(WhatsappMessageStatus.FAILED);
    }

    @Test
    void shouldReturnExistingMessageIfAlreadyPrepared() {
        Professional pro = professionalRepository.save(TestProfessionalFactory.builder().build());
        Client client = clientRepository.save(TestClientFactory.builder().build());
        SalonService service = salonServiceRepository.save(TestSalonServiceFactory.builder().build());
        Appointment appointment = appointmentRepository.save(TestAppointmentFactory.standardForIt(client, pro, service));

        WhatsappMessage first = whatsappMessageService.prepareAppointmentMessage(appointment.getId(), WhatsappMessageType.CONFIRMATION);

        WhatsappMessage second = whatsappMessageService.prepareAppointmentMessage(appointment.getId(), WhatsappMessageType.CONFIRMATION);

        assertThat(second.getId()).isEqualTo(first.getId());
    }

    @Test
    void shouldPrepareRetentionMessage() {
        Professional pro = professionalRepository.save(TestProfessionalFactory.builder().build());
        Client client = clientRepository.save(TestClientFactory.builder().build());
        SalonService service = salonServiceRepository.save(TestSalonServiceFactory.builder().build());
        RetentionForecast forecast = retentionForecastRepository.save(TestRetentionForecastFactory.standardForIt(pro, client, List.of(service)));

        WhatsappMessage message = whatsappMessageService.prepareRetentionMessage(forecast.getId(), WhatsappMessageType.RETENTION_MAINTENANCE);

        assertThat(message.getRetentionForecast().getId()).isEqualTo(forecast.getId());
        assertThat(message.getMessageType()).isEqualTo(WhatsappMessageType.RETENTION_MAINTENANCE);
    }

    @Test
    void shouldUpdateMessageStatus() {
        Professional pro = professionalRepository.save(TestProfessionalFactory.builder().build());
        Client client = clientRepository.save(TestClientFactory.builder().build());
        SalonService service = salonServiceRepository.save(TestSalonServiceFactory.builder().build());
        Appointment appointment = appointmentRepository.save(TestAppointmentFactory.standardForIt(client, pro, service));
        WhatsappMessage message = whatsappMessageService.prepareAppointmentMessage(appointment.getId(), WhatsappMessageType.CONFIRMATION);

        whatsappMessageService.updateMessageStatus(WhatsappMessageStatus.SENT, null, "ext-id-123", message.getId());

        WhatsappMessage updated = whatsappMessageRepository.findById(message.getId()).orElseThrow();
        assertThat(updated.getMessageStatus()).isEqualTo(WhatsappMessageStatus.SENT);
        assertThat(updated.getExternalMessageId()).isEqualTo("ext-id-123");
        assertThat(updated.getAttempts()).isEqualTo(1);
        assertThat(updated.getSentAt()).isNotNull();
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
        
        try {
            whatsappMessageService.prepareAppointmentMessage(appointmentA.getId(), WhatsappMessageType.CONFIRMATION);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).isEqualTo("Appointment not found");
        }
    }
}
