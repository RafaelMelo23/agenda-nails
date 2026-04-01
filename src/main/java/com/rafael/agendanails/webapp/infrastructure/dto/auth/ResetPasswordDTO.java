package com.rafael.agendanails.webapp.infrastructure.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordDTO(
        @NotBlank(message = "O e-mail do usuário é obrigatório")
        @Email(message = "O e-mail deve ser válido")
        String userEmail,

        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 8, message = "A nova senha deve ter pelo menos 8 caracteres")
        String newPassword,

        @NotBlank(message = "O token de redefinição é obrigatório")
        String resetToken
) {
}
