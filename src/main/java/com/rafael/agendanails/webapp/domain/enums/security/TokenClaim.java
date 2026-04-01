package com.rafael.agendanails.webapp.domain.enums.security;

import lombok.Getter;

@Getter
public enum TokenClaim {

    EMAIL("email"),
    ROLE("role"),
    TENANT_ID("tenantId");

    private final String value;

    TokenClaim(String value) {
        this.value = value;
    }
}
