package com.rafael.nailspro.webapp.infrastructure.controller.api.auth;

import com.rafael.nailspro.webapp.application.auth.AuthenticationService;
import com.rafael.nailspro.webapp.domain.model.UserPrincipal;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.AuthResultDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.LoginDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.RegisterDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.TokenRefreshResponseDTO;
import com.rafael.nailspro.webapp.infrastructure.security.token.CookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Optional;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final CookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDTO loginDTO,
                                        HttpServletResponse response) {

        AuthResultDTO authResultDTO = authenticationService.login(loginDTO);

        cookieService.addRefreshTokenCookie(response, authResultDTO.refreshToken());

        return ResponseEntity.ok().body(authResultDTO.jwtToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                       HttpServletRequest request, HttpServletResponse response) {

        if (Arrays.stream(request.getCookies()).findAny().isPresent()) {
            Optional<Cookie> refreshToken = Arrays.stream(request.getCookies())
                    .filter(c -> "refresh_token".equalsIgnoreCase(c.getName()))
                    .findFirst();

            if (refreshToken.isPresent()) {
                authenticationService.logout(refreshToken.get().getValue(), userPrincipal.getUserId());
                cookieService.deleteRefreshTokenCookie(response);
            }

            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {

        authenticationService.register(registerDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@CookieValue(name = "refresh_token") String refreshToken,
                                          HttpServletResponse response) {

        TokenRefreshResponseDTO tokenRefreshResponseDTO = authenticationService.refreshToken(refreshToken);

        cookieService.addRefreshTokenCookie(response, tokenRefreshResponseDTO.refreshToken());

        return ResponseEntity.ok(tokenRefreshResponseDTO.jwtToken());
    }
}