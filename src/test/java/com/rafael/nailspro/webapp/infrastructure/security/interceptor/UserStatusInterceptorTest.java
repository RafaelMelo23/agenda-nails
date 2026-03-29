package com.rafael.nailspro.webapp.infrastructure.security.interceptor;

import com.rafael.nailspro.webapp.domain.enums.user.UserStatus;
import com.rafael.nailspro.webapp.domain.model.Client;
import com.rafael.nailspro.webapp.domain.model.UserPrincipal;
import com.rafael.nailspro.webapp.domain.repository.UserRepository;
import com.rafael.nailspro.webapp.shared.tenant.TenantContext;
import com.rafael.nailspro.webapp.support.factory.TestClientFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserStatusInterceptorTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserStatusInterceptor userStatusInterceptor;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void preHandle_handlesActiveUser() {
        Client activeClient = TestClientFactory.standard();
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .email(activeClient.getEmail())
                .tenantId(activeClient.getTenantId())
                .userRole(activeClient.getUserRole())
                .userId(activeClient.getId()).build();

        var authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities());

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.existsByIdAndStatus(any(), eq(UserStatus.BANNED)))
                .thenReturn(false);

        boolean preHandle = userStatusInterceptor.preHandle(request, response, new Object());

        assertThat(preHandle).isTrue();
    }

    @Test
    void preHandle_handlesBannedUser() {
        Client activeClient = TestClientFactory.standard();
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .email(activeClient.getEmail())
                .tenantId(activeClient.getTenantId())
                .userRole(activeClient.getUserRole())
                .userId(activeClient.getId()).build();

        var authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities());

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.existsByIdAndStatus(any(), eq(UserStatus.BANNED)))
                .thenReturn(true);

        boolean preHandle = userStatusInterceptor.preHandle(request, response, new Object());

        assertThat(preHandle).isFalse();
    }

    @Test
    void preHandle_handlesAnonymousUser() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication anonymousAuth = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        when(securityContext.getAuthentication()).thenReturn(anonymousAuth);
        SecurityContextHolder.setContext(securityContext);

        boolean preHandle = userStatusInterceptor.preHandle(request, response, new Object());

        assertThat(preHandle).isTrue();
    }
}
