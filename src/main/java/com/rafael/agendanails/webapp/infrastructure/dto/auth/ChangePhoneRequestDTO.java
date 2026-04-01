package com.rafael.agendanails.webapp.infrastructure.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ChangePhoneRequestDTO(
        @NotBlank(message = "O novo telefone é obrigatório")
        String newPhone,

        @NotBlank(message = "A senha é obrigatória")
        String password
) {
}
