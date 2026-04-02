package com.rafael.agendanails.webapp.domain.enums.security;

import lombok.Getter;

@Getter
public enum TokenClaim {

    EMAIL("email"),
    ROLE("roles"),
    TENANT_ID("tenantId"),
    PURPOSE("purpose"),
    FIRST_LOGIN("isFirstLogin");

    private final String value;

    TokenClaim(String value) {
        this.value = value;
    }
}
