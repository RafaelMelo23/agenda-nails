package com.rafael.agendanails.webapp.infrastructure.dto.sse;

import com.rafael.agendanails.webapp.domain.enums.SseEventType;
import lombok.Builder;

@Builder
public record SsePayloadDTO(SseEventType sseEventType,
                            Object data) {
}
