package com.rafael.agendanails.webapp.infrastructure.dto.admin.professional;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateProfessionalDTO(
        Long id,

        @NotBlank(message = "O nome do profissional é obrigatório")
        String fullName,

        @NotBlank(message = "O e-mail do profissional é obrigatório")
        @Email(message = "O e-mail do profissional deve ser válido")
        String email,

        @NotEmpty(message = "Pelo menos um serviço deve ser informado")
        List<Long> servicesOfferedByProfessional
) {}