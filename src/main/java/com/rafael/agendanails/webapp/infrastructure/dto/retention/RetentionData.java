package com.rafael.agendanails.webapp.infrastructure.dto.retention;

import com.rafael.agendanails.webapp.domain.model.RetentionForecast;
import com.rafael.agendanails.webapp.domain.model.WhatsappMessage;

public record RetentionData(
        RetentionForecast forecast,
        WhatsappMessage messageRecord,
        String messageContent
) {}
