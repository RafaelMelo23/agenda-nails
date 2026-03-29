package com.rafael.nailspro.webapp.application.auth;

import com.rafael.nailspro.webapp.domain.enums.user.UserRole;
import com.rafael.nailspro.webapp.domain.enums.user.UserStatus;
import com.rafael.nailspro.webapp.domain.model.Client;
import com.rafael.nailspro.webapp.domain.model.RefreshToken;
import com.rafael.nailspro.webapp.domain.repository.ClientRepository;
import com.rafael.nailspro.webapp.domain.repository.UserRepository;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.AuthResultDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.LoginDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.RegisterDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.TokenRefreshResponseDTO;
import com.rafael.nailspro.webapp.infrastructure.exception.BusinessException;
import com.rafael.nailspro.webapp.infrastructure.exception.TokenRefreshException;
import com.rafael.nailspro.webapp.infrastructure.exception.UserAlreadyExistsException;
import com.rafael.nailspro.webapp.infrastructure.security.token.TokenService;
import com.rafael.nailspro.webapp.infrastructure.security.token.refresh.RefreshTokenService;
import com.rafael.nailspro.webapp.shared.tenant.TenantContext;
import com.rafael.nailspro.webapp.support.factory.TestClientFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private static final String DEFAULT_TENANT = "tenant-test";

    @BeforeEach
    void setUp() {
        TenantContext.setTenant(DEFAULT_TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should register a new client successfully")
    void shouldRegisterNewClientSuccessfully() {
        RegisterDTO dto = new RegisterDTO("Test Name", "email@test.com", "password123", "11999999999");
        when(userRepository.findByEmailIgnoreCase(dto.email())).thenReturn(Optional.empty());
        when(clientRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.rawPassword())).thenReturn("encodedPassword");

        authenticationService.register(dto);

        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists during registration")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterDTO dto = new RegisterDTO("Test Name", "email@test.com", "password123", "11999999999");
        when(userRepository.findByEmailIgnoreCase(dto.email())).thenReturn(Optional.of(TestClientFactory.standard()));

        assertThatThrownBy(() -> authenticationService.register(dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("O E-mail informado já está sendo utilizado");

        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Should throw exception when phone number already exists during registration")
    void shouldThrowExceptionWhenPhoneNumberAlreadyExists() {
        RegisterDTO dto = new RegisterDTO("Test Name", "email@test.com", "password123", "11999999999");
        when(userRepository.findByEmailIgnoreCase(dto.email())).thenReturn(Optional.empty());
        when(clientRepository.findByPhoneNumber(anyString())).thenReturn(Optional.of(TestClientFactory.standard()));

        assertThatThrownBy(() -> authenticationService.register(dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("O telefone informado já está sendo utilizado");

        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Should login successfully and return tokens")
    void shouldLoginSuccessfully() {
        LoginDTO loginDTO = new LoginDTO("email@test.com", "password");
        Client user = TestClientFactory.builder()
                .email(loginDTO.email())
                .password("encodedPassword")
                .tenantId(DEFAULT_TENANT)
                .build();

        when(userRepository.findByEmailIgnoreCase(loginDTO.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDTO.password(), user.getPassword())).thenReturn(true);
        when(tokenService.generateAuthToken(user)).thenReturn("jwt-token");
        
        RefreshToken refreshToken = RefreshToken.builder().token("refresh-token").build();
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        AuthResultDTO result = authenticationService.login(loginDTO);

        assertThat(result.jwtToken()).isEqualTo("jwt-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    @DisplayName("Should throw exception when user not found during login")
    void shouldThrowExceptionWhenUserNotFound() {
        LoginDTO loginDTO = new LoginDTO("email@test.com", "password");
        when(userRepository.findByEmailIgnoreCase(loginDTO.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.login(loginDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Os dados informados são inválidos");

        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when password does not match during login")
    void shouldThrowExceptionWhenPasswordDoesNotMatch() {
        LoginDTO loginDTO = new LoginDTO("email@test.com", "password");
        Client user = TestClientFactory.standard();
        
        when(userRepository.findByEmailIgnoreCase(loginDTO.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDTO.password(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.login(loginDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Os dados informados são inválidos");
    }

    @Test
    @DisplayName("Should throw exception when user is banned during login")
    void shouldThrowExceptionWhenUserIsBanned() {
        LoginDTO loginDTO = new LoginDTO("email@test.com", "password");
        Client user = TestClientFactory.builder()
                .status(UserStatus.BANNED)
                .build();

        when(userRepository.findByEmailIgnoreCase(loginDTO.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDTO.password(), user.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.login(loginDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Você foi banido deste estabelecimento");
    }

    @Test
    @DisplayName("Should throw exception when user belongs to different tenant during login")
    void shouldThrowExceptionWhenUserBelongsToDifferentTenant() {
        LoginDTO loginDTO = new LoginDTO("email@test.com", "password");
        Client user = TestClientFactory.builder()
                .tenantId("other-tenant")
                .userRole(UserRole.CLIENT)
                .build();

        when(userRepository.findByEmailIgnoreCase(loginDTO.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDTO.password(), user.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.login(loginDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Acesso negado para este estabelecimento.");
    }

    @Test
    @DisplayName("Should allow login for SUPER_ADMIN even with different tenant")
    void shouldAllowSuperAdminLoginRegardlessOfTenant() {
        LoginDTO loginDTO = new LoginDTO("admin@test.com", "password");
        Client user = TestClientFactory.builder()
                .tenantId("other-tenant")
                .userRole(UserRole.SUPER_ADMIN)
                .build();

        when(userRepository.findByEmailIgnoreCase(loginDTO.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDTO.password(), user.getPassword())).thenReturn(true);
        when(tokenService.generateAuthToken(user)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(RefreshToken.builder().token("refresh-token").build());

        AuthResultDTO result = authenticationService.login(loginDTO);

        assertThat(result.jwtToken()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("Should revoke token on logout")
    void shouldRevokeTokenOnLogout() {
        authenticationService.logout("refresh-token", 1L);
        verify(refreshTokenService).revokeUserToken("refresh-token", 1L);
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() {
        String oldTokenStr = "old-refresh-token";
        Client user = TestClientFactory.standard();
        RefreshToken oldToken = RefreshToken.builder()
                .token(oldTokenStr)
                .user(user)
                .isRevoked(false)
                .build();

        when(refreshTokenService.findByToken(oldTokenStr)).thenReturn(Optional.of(oldToken));
        when(refreshTokenService.verifyExpiration(oldToken)).thenReturn(oldToken);
        when(refreshTokenService.createRefreshToken(user)).thenReturn(RefreshToken.builder().token("new-refresh-token").build());
        when(tokenService.generateAuthToken(user)).thenReturn("new-jwt-token");

        TokenRefreshResponseDTO result = authenticationService.refreshToken(oldTokenStr);

        assertThat(result.jwtToken()).isEqualTo("new-jwt-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(oldToken.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("Should revoke all tokens and throw exception if token is already revoked")
    void shouldRevokeAllAndThrowExceptionIfTokenAlreadyRevoked() {
        String tokenStr = "revoked-token";
        Client user = TestClientFactory.builder().id(1L).build();
        RefreshToken token = RefreshToken.builder()
                .token(tokenStr)
                .user(user)
                .isRevoked(true)
                .build();

        when(refreshTokenService.findByToken(tokenStr)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authenticationService.refreshToken(tokenStr))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("Este token já foi utilizado.");

        verify(refreshTokenService).revokeAllForUser(1L);
    }

    @Test
    @DisplayName("Should throw exception if token not found")
    void shouldThrowExceptionIfTokenNotFound() {
        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.refreshToken("non-existent"))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("Token não encontrado");
    }
}