package com.rafael.agendanails.webapp.infrastructure.controller.api.auth;

import com.rafael.agendanails.webapp.application.auth.DemoAuthenticationService;
import com.rafael.agendanails.webapp.domain.enums.demo.DemoUserType;
import com.rafael.agendanails.webapp.infrastructure.dto.auth.AuthResultDTO;
import com.rafael.agendanails.webapp.infrastructure.dto.auth.LoginResponseDTO;
import com.rafael.agendanails.webapp.infrastructure.security.token.CookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/demo")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class DemoAuthController {

    private final DemoAuthenticationService demoService;
    private final CookieService cookieService;

    @PostMapping
    @Operation(summary = "Gera um acesso administrativo temporário para demonstração")
    public ResponseEntity<LoginResponseDTO> enterDemoMode(@RequestBody DemoUserType userType,
                                                          HttpServletResponse response) {
        AuthResultDTO result = demoService.createAndLoginDemoUser(userType);

        cookieService.addRefreshTokenCookie(response, result.refreshToken());
        return ResponseEntity.ok(new LoginResponseDTO(result.jwtToken()));
    }
}