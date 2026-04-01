package com.rafael.agendanails.webapp.infrastructure.dto.auth;

public record TokenRefreshResponseDTO(
        String jwtToken,
        String refreshToken,
        String tokenType
) {
    public TokenRefreshResponseDTO(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, "Bearer");
    }
}
