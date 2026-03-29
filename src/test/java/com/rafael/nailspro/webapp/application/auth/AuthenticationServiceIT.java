package com.rafael.nailspro.webapp.application.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafael.nailspro.webapp.domain.enums.user.UserStatus;
import com.rafael.nailspro.webapp.domain.model.Client;
import com.rafael.nailspro.webapp.domain.model.RefreshToken;
import com.rafael.nailspro.webapp.domain.model.User;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.AuthResultDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.LoginDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.RegisterDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.TokenRefreshResponseDTO;
import com.rafael.nailspro.webapp.infrastructure.exception.BusinessException;
import com.rafael.nailspro.webapp.infrastructure.exception.TokenRefreshException;
import com.rafael.nailspro.webapp.infrastructure.security.token.TokenService;
import com.rafael.nailspro.webapp.shared.tenant.TenantContext;
import com.rafael.nailspro.webapp.support.BaseIntegrationTest;
import com.rafael.nailspro.webapp.support.factory.TestClientFactory;
import jakarta.persistence.EntityManager;
import org.antlr.v4.runtime.atn.TokensStartState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthenticationServiceIT extends BaseIntegrationTest {

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private TokenService tokenService;

    @Test
    void shouldRegisterSuccessfullyIT() {
        RegisterDTO dto = new RegisterDTO("IT Test User", "it-test@example.com", "securePass123", "11988887777");

        authenticationService.register(dto);

        Optional<User> persistedUser = userRepository.findByEmailIgnoreCase("it-test@example.com");

        assertThat(persistedUser).isPresent();
        assertThat(persistedUser.get().getFullName()).isEqualTo("IT Test User");

        assertThat(passwordEncoder.matches("securePass123", persistedUser.get().getPassword())).isTrue();
    }

    @Test
    void shouldFailLoginWhenPasswordIsWrongIT() {
        Client client = TestClientFactory.standardForIt();
        client.setPassword(passwordEncoder.encode("whatever"));
        userRepository.save(client);

        Optional<User> persistedUser = userRepository.findByEmailIgnoreCase(client.getEmail());

        assertThat(persistedUser).isPresent();

        assertThrows(BusinessException.class, () -> authenticationService.login(new LoginDTO(client.getEmail(), "wrongPassword")));
    }

    @Test
    void shouldGenerateAndPersistRefreshTokenOnLoginIT() {
        Client client = TestClientFactory.standardForIt();
        client.setPassword(passwordEncoder.encode("whatever"));
        userRepository.save(client);

        Optional<User> persistedUser = userRepository.findByEmailIgnoreCase(client.getEmail());
        assertThat(persistedUser).isPresent();

        AuthResultDTO result = authenticationService.login(new LoginDTO(client.getEmail(), "whatever"));
        assertThat(result.jwtToken()).isNotBlank();

        Optional<RefreshToken> refresh = refreshTokenRepository.findByToken(result.refreshToken());

        assertThat(refresh).isPresent();
    }

    @Test
    void shouldFailLoginWhenTenantMismatches() {
        TenantContext.clear();
        TenantContext.setTenant("tenant-b");
        Client client = TestClientFactory.standardForIt();
        client.setPassword(passwordEncoder.encode("whatever"));
        userRepository.save(client);

        assertThrows(BusinessException.class, () -> authenticationService.login(new LoginDTO(client.getEmail(), "wrongPassword")));
    }

    @Test
    void shouldRevokeAllTokensWhenAnExpiredOneIsUsed() {
        Client client = TestClientFactory.standardForIt();
        client.setPassword(passwordEncoder.encode("whatever"));
        userRepository.save(client);

        RefreshToken token1 = RefreshToken.builder().token("token1").user(client).expiryDate(Instant.now().plusSeconds(1000)).isRevoked(false).build();
        RefreshToken token2 = RefreshToken.builder().token("token2").user(client).expiryDate(Instant.now().plusSeconds(1000)).isRevoked(false).build();
        RefreshToken token3 = RefreshToken.builder().token("token3").user(client).expiryDate(Instant.now().plusSeconds(1000)).isRevoked(true).build();
        refreshTokenRepository.saveAll(List.of(token1, token2, token3));

        assertThatThrownBy(() -> authenticationService.refreshToken("token3"))
                .isInstanceOf(TokenRefreshException.class);

        entityManager.flush();

        List<RefreshToken> all = refreshTokenRepository.findAll();

        assertThat(all).extracting(RefreshToken::isRevoked).containsOnly(true);
    }

    @Test
    void shouldDenyLoginForBannedUser() throws Exception {
        Client client = TestClientFactory.standardForIt();
        client.setPassword(passwordEncoder.encode("whatever"));
        client.setStatus(UserStatus.BANNED);
        userRepository.save(client);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(new LoginDTO(client.getEmail(), "whatever"))))
                .andExpect(status().isUnauthorized());

    }

    @Test
    void shouldRefreshUserTokenSuccessfullyIT() {
        Client client = TestClientFactory.standardForIt();
        client.setPassword(passwordEncoder.encode("whatever"));
        userRepository.save(client);

        RefreshToken refreshToken = RefreshToken.builder()
                .token("old-token")
                .user(client)
                .expiryDate(Instant.now().plusSeconds(1000))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        TokenRefreshResponseDTO response = authenticationService.refreshToken("old-token");

        assertThat(response.jwtToken()).isNotEmpty();
        assertThat(response.refreshToken()).isNotEqualTo("old-token");

        RefreshToken updatedOldToken = refreshTokenRepository.findByToken("old-token").orElseThrow();
        assertThat(updatedOldToken.isRevoked()).isTrue();

        RefreshToken persistedNewToken = refreshTokenRepository.findByToken(response.refreshToken()).orElseThrow();

        assertThat(persistedNewToken)
                .isNotNull();
    }
}