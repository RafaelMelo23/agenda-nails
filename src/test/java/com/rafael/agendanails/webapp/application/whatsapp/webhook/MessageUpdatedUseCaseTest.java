package com.rafael.agendanails.webapp.application.whatsapp.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafael.agendanails.webapp.domain.enums.evolution.EvolutionMessageStatus;
import com.rafael.agendanails.webapp.domain.enums.whatsapp.WhatsappMessageStatus;
import com.rafael.agendanails.webapp.domain.model.WhatsappMessage;
import com.rafael.agendanails.webapp.domain.repository.WhatsappMessageRepository;
import com.rafael.agendanails.webapp.infrastructure.dto.whatsapp.evolution.webhook.EvolutionWebhookResponseDTO;
import com.rafael.agendanails.webapp.infrastructure.dto.whatsapp.evolution.webhook.message.update.MessageUpdateData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageUpdatedUseCaseTest {

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private WhatsappMessageRepository messageRepository;

    @InjectMocks
    private MessageUpdatedUseCase messageUpdatedUseCase;

    @Test
    void shouldUpdateMessageStatus() {
        String messageId = "msg-123";
        MessageUpdateData data = new MessageUpdateData(messageId, EvolutionMessageStatus.SERVER_ACK);
        EvolutionWebhookResponseDTO<MessageUpdateData> response = EvolutionWebhookResponseDTO.<MessageUpdateData>builder()
                .instance("tenant-1")
                .data(data)
                .build();

        WhatsappMessage message = WhatsappMessage.builder()
                .externalMessageId(messageId)
                .build();

        when(messageRepository.findByExternalMessageId(messageId)).thenReturn(Optional.of(message));

        messageUpdatedUseCase.process(response);

        verify(messageRepository).save(message);
        assert message.getMessageStatus() == WhatsappMessageStatus.SENT;
    }

    @Test
    void shouldDoNothingIfMessageNotFound() {
        String messageId = "msg-404";
        MessageUpdateData data = new MessageUpdateData(messageId, EvolutionMessageStatus.SERVER_ACK);
        EvolutionWebhookResponseDTO<MessageUpdateData> response = EvolutionWebhookResponseDTO.<MessageUpdateData>builder()
                .instance("tenant-1")
                .data(data)
                .build();

        when(messageRepository.findByExternalMessageId(messageId)).thenReturn(Optional.empty());

        messageUpdatedUseCase.process(response);

        verify(messageRepository, never()).save(any());
    }
}
