package com.rafael.agendanails.webapp.infrastructure.controller.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafael.agendanails.webapp.application.auth.DemoAuthenticationService;
import com.rafael.agendanails.webapp.domain.enums.demo.DemoUserType;
import com.rafael.agendanails.webapp.infrastructure.dto.auth.AuthResultDTO;
import com.rafael.agendanails.webapp.infrastructure.security.token.CookieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DemoAuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DemoAuthenticationService demoService;

    @Mock
    private CookieService cookieService;

    @InjectMocks
    private DemoAuthController demoAuthController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(demoAuthController).build();
    }

    @Test
    void shouldEnterDemoModeAsClient() throws Exception {
        AuthResultDTO authResult = new AuthResultDTO("jwt-token", "refresh-token");
        when(demoService.createAndLoginDemoUser(DemoUserType.CLIENT)).thenReturn(authResult);

        mockMvc.perform(post("/api/v1/auth/demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(DemoUserType.CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").value("jwt-token"));

        verify(demoService).createAndLoginDemoUser(DemoUserType.CLIENT);
        verify(cookieService).addRefreshTokenCookie(any(), eq("refresh-token"));
    }

    @Test
    void shouldEnterDemoModeAsProfessional() throws Exception {
        AuthResultDTO authResult = new AuthResultDTO("jwt-token", "refresh-token");
        when(demoService.createAndLoginDemoUser(DemoUserType.PROFESSIONAL)).thenReturn(authResult);

        mockMvc.perform(post("/api/v1/auth/demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(DemoUserType.PROFESSIONAL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").value("jwt-token"));

        verify(demoService).createAndLoginDemoUser(DemoUserType.PROFESSIONAL);
    }
}
