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

    private static final String CLIENT_MANAGE_APPOINTMENTS_PAGE_URL = "todo/{id}/cancelar"; //todo
    private static final String BOOK_APPOINTMENT_PAGE_URL = ""; //todo

    public String buildBaseUrl(String tenantId) {

        return UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(tenantId + "." + domain)
                .build()
                .toUriString();
    }

    /* todo: might change this for a more generalistic URL,
        like a manager page, that maybe, perhaps,
        will also have a path variable as it shows right now, to direct the user
        to that specific appointment already
    **/
    public String buildCancelAppointmentUrl(String tenantId, Long appointmentId) {
        String baseUrl = buildBaseUrl(tenantId);

        return UriComponentsBuilder.fromUriString(baseUrl)
                .path(CLIENT_MANAGE_APPOINTMENTS_PAGE_URL)
                .buildAndExpand(appointmentId)
                .toUriString();
    }

    public String buildBookAppointmentUrl(String tenantId) {
        String baseUrl = buildBaseUrl(tenantId);

        return UriComponentsBuilder.fromUriString(baseUrl)
                .path(BOOK_APPOINTMENT_PAGE_URL)
                .toUriString();
    }
}
