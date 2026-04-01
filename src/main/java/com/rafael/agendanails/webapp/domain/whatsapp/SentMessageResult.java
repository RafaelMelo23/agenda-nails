package com.rafael.agendanails.webapp.domain.whatsapp;

import com.rafael.agendanails.webapp.domain.enums.evolution.EvolutionMessageStatus;
import lombok.Builder;

@Builder
public record SentMessageResult(String messageId,
                                EvolutionMessageStatus status) {
}
