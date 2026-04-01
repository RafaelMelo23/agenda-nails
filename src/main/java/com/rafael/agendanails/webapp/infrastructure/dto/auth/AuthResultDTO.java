package com.rafael.agendanails.webapp.infrastructure.dto.auth;

import lombok.Builder;

@Builder
public record AuthResultDTO(String jwtToken,
                            String refreshToken) {
}
