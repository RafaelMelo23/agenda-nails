package com.rafael.agendanails.webapp.infrastructure.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class TenantUrlProvider {

    @Value("${app.protocol.http}")
    private String protocol;

    @Value("${domain.url}")
    private String domain;

    private static final String CLIENT_MANAGE_APPOINTMENTS_PAGE_URL = "/perfil";
    private static final String BOOK_APPOINTMENT_PAGE_URL = "/agendar";

    public String buildBaseUrl(String tenantId) {

        return UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(tenantId + "." + domain)
                .build()
                .toUriString();
    }

    public String buildCancelAppointmentUrl(String tenantId, Long appointmentId) {
        String baseUrl = buildBaseUrl(tenantId);

        return UriComponentsBuilder.fromUriString(baseUrl)
                .path(CLIENT_MANAGE_APPOINTMENTS_PAGE_URL)
                .queryParam("id", appointmentId)
                .toUriString();
    }

    public String buildBookAppointmentUrl(String tenantId) {
        String baseUrl = buildBaseUrl(tenantId);

        return UriComponentsBuilder.fromUriString(baseUrl)
                .path(BOOK_APPOINTMENT_PAGE_URL)
                .toUriString();
    }
}
