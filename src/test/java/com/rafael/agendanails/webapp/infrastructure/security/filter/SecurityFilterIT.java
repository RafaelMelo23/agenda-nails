package com.rafael.agendanails.webapp.infrastructure.security.filter;

import com.rafael.agendanails.webapp.infrastructure.security.token.TokenService;
import com.rafael.agendanails.webapp.support.BaseIntegrationTest;
import com.rafael.agendanails.webapp.support.factory.TestClientFactory;
import com.rafael.agendanails.webapp.support.factory.TestProfessionalFactory;
import com.rafael.agendanails.webapp.support.factory.TestSalonProfileFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityFilterIT extends BaseIntegrationTest {

    @Autowired
    private TokenService tokenService;
    @Autowired
    private MockMvc mvc;

    @Test
    void shouldAllowAccessWhenUserIsAuthenticatedWithValidToken() throws Exception {
        var professional = professionalRepository.save(TestProfessionalFactory.standardForIt());
        salonProfileRepository.save(TestSalonProfileFactory.standardForIT(professional, "tenant-test"));
        var client = clientRepository.save(TestClientFactory.standardForIt());

        String token = tokenService.generateAuthToken(client);

        mvc.perform(get("/api/v1/professional/simplified")
                        .header("X-Tenant-Id", "tenant-test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectAccessWhenTokenIsMissing() throws Exception {
        var professional = professionalRepository.save(TestProfessionalFactory.standardForIt());
        salonProfileRepository.save(TestSalonProfileFactory.standardForIT(professional, "tenant-test"));
        mvc.perform(get("/api/v1/user")
                .header("X-Tenant-Id", "tenant-test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessToWebhookWithoutToken() throws Exception {
        mvc.perform(post("/api/v1/webhook")
                        .header("apiKey", "test")
                        .header("X-Tenant-Id", "tenant-test")
                        .content("{}")
                        .contentType("application/json"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("Expected access to be allowed, but was " + status);
                    }
                });
    }

    @Test
    void shouldIgnoreTokenAndRejectAccessWhenTokenPurposeIsIncorrect() throws Exception {
        var professional = professionalRepository.save(TestProfessionalFactory.standardForIt());
        salonProfileRepository.save(TestSalonProfileFactory.standardForIT(professional, "tenant-test"));
        var client = clientRepository.save(TestClientFactory.standardForIt());

        String token = tokenService.generateResetPasswordToken(client.getId());

        mvc.perform(get("/api/v1/user")
                        .header("X-Tenant-Id", "tenant-test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectAccessWhenUserRoleIsInsufficientForResource() throws Exception {
        var professional = professionalRepository.save(TestProfessionalFactory.standardForIt());
        salonProfileRepository.save(TestSalonProfileFactory.standardForIT(professional, "tenant-test"));
        var client = clientRepository.save(TestClientFactory.standardForIt());

        String token = tokenService.generateAuthToken(client);

        mvc.perform(get("/api/v1/admin/appointments/users/" + client.getId())
                        .header("X-Tenant-Id", "tenant-test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCorrectlyMapPrincipalWhenUserIsAuthenticated() throws Exception {
        var professional = professionalRepository.save(TestProfessionalFactory.standardForIt());
        salonProfileRepository.save(TestSalonProfileFactory.standardForIT(professional));

        String token = tokenService.generateAuthToken(professional);

        mvc.perform(get("/api/v1/professional/schedule/block")
                        .param("dateAndTime", ZonedDateTime.now().toString())
                        .header(HttpHeaders.HOST, "tenant-test.localhost")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }
}
