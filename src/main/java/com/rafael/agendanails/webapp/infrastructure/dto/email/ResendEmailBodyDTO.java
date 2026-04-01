package com.rafael.agendanails.webapp.infrastructure.dto.email;

import lombok.Builder;

import java.util.List;

@Builder
public record ResendEmailBodyDTO(String from,
                                 List<String> to,
                                 String subject,
                                 String html) {
}