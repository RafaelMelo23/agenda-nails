package com.rafael.agendanails.webapp.infrastructure.dto.user.profile;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public abstract class UserProfileDto {
    private String fullName;
    private String email;
    private String role;
}
