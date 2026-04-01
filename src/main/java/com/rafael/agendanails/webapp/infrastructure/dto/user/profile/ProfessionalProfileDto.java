package com.rafael.agendanails.webapp.infrastructure.dto.user.profile;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class ProfessionalProfileDto extends UserProfileDto {
    private String professionalPicture;
    private UUID externalId;
    private Boolean isActive;
}
