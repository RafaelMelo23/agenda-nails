package com.rafael.agendanails.webapp.infrastructure.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeEmailRequestDTO(
        @NotBlank(message = "O novo e-mail é obrigatório")
        @Email(message = "O novo e-mail deve ser válido")
        String newEmail,

        @NotBlank(message = "A senha é obrigatória")
        String password
) {
}
