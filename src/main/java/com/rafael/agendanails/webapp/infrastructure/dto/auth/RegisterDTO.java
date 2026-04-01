package com.rafael.agendanails.webapp.infrastructure.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterDTO(
        @NotBlank(message = "O nome completo é obrigatório")
        String fullName,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail deve ser válido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
        String rawPassword,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "O telefone deve conter 11 dígitos (DDD + número)")
        String phoneNumber
) {}
