package com.rafael.nailspro.webapp.infrastructure.dto.user.profile;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class ClientProfileDto extends UserProfileDto {
    private String phoneNumber;
    private Integer missedAppointments;
    private Integer canceledAppointments;
}
