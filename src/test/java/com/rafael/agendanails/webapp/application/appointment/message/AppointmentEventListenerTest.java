package com.rafael.agendanails.webapp.application.appointment.message;

import com.rafael.agendanails.webapp.infrastructure.dto.appointment.booking.event.AppointmentConfirmedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppointmentEventListenerTest {

    @Mock
    private AppointmentMessagingUseCase messagingUseCase;

    @InjectMocks
    private AppointmentEventListener appointmentEventListener;

    @Test
    void shouldCallMessagingUseCaseWhenAppointmentConfirmedEventIsReceived() {
        AppointmentConfirmedEvent event = new AppointmentConfirmedEvent(1L);

        appointmentEventListener.handleConfirmedAppointment(event);

        verify(messagingUseCase).sendAppointmentConfirmationMessage(1L);
    }
}
