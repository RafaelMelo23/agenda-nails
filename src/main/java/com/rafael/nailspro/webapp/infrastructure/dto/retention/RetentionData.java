package com.rafael.nailspro.webapp.infrastructure.dto.retention;

import com.rafael.nailspro.webapp.domain.model.RetentionForecast;
import com.rafael.nailspro.webapp.domain.model.WhatsappMessage;

public record RetentionData(
        RetentionForecast forecast,
        WhatsappMessage messageRecord,
        String messageContent
) {}
