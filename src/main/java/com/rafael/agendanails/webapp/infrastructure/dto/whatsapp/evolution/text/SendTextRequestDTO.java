package com.rafael.agendanails.webapp.infrastructure.dto.whatsapp.evolution.text;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendTextRequestDTO(
        String number,
        String text,
        Integer delay,
        Boolean linkPreview,
        Boolean mentionsEveryOne

) {

    public static SendTextRequestDTO of(String phone, String message) {
        return SendTextRequestDTO.builder()
                .number(phone)
                .text(message)
                .linkPreview(false)
                .build();
    }
}
