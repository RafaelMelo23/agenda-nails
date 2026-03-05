package com.rafael.nailspro.webapp.application.whatsapp.response;

import com.rafael.nailspro.webapp.domain.enums.evolution.EvolutionMessageStatus;
import lombok.Builder;

@Builder
public record SentMessageResult(String messageId,
                                EvolutionMessageStatus status) {
}
