package com.rafael.agendanails.webapp.application.auth;

import com.rafael.agendanails.webapp.domain.enums.demo.DemoUserType;
import com.rafael.agendanails.webapp.domain.model.Client;
import com.rafael.agendanails.webapp.domain.model.Professional;
import com.rafael.agendanails.webapp.domain.model.User;
import com.rafael.agendanails.webapp.domain.repository.UserRepository;
import com.rafael.agendanails.webapp.infrastructure.dto.auth.AuthResultDTO;
import com.rafael.agendanails.webapp.infrastructure.dto.auth.LoginDTO;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemoAuthenticationServiceTest {

    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DemoAuthenticationService demoAuthenticationService;

    private static final String DEMO_TENANT = "demo-salon-2026";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(demoAuthenticationService, "demoTenant", DEMO_TENANT);
    }

    @Test
    void shouldCreateAndLoginDemoClient() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(authenticationService.login(any(LoginDTO.class)))
                .thenReturn(new AuthResultDTO("jwt", "refresh"));

        AuthResultDTO result = demoAuthenticationService.createAndLoginDemoUser(DemoUserType.CLIENT);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser).isInstanceOf(Client.class);
        assertThat(savedUser.getTenantId()).isEqualTo(DEMO_TENANT);
        assertThat(savedUser.getEmail()).contains("demo.client");
        
        verify(entityManager).flush();
        verify(authenticationService).login(argThat(login -> 
            login.email().equals(savedUser.getEmail()) && login.password().equals("123456")
        ));
        
        assertThat(result.jwtToken()).isEqualTo("jwt");
    }

    @Test
    void shouldCreateAndLoginDemoProfessional() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(authenticationService.login(any(LoginDTO.class)))
                .thenReturn(new AuthResultDTO("jwt", "refresh"));

        AuthResultDTO result = demoAuthenticationService.createAndLoginDemoUser(DemoUserType.PROFESSIONAL);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser).isInstanceOf(Professional.class);
        assertThat(savedUser.getTenantId()).isEqualTo(DEMO_TENANT);
        assertThat(savedUser.getEmail()).contains("demo.profissional");
        
        Professional professional = (Professional) savedUser;
        assertThat(professional.getWorkSchedules()).isNotEmpty();
        
        verify(entityManager).flush();
        verify(authenticationService).login(argThat(login -> 
            login.email().equals(savedUser.getEmail()) && login.password().equals("123456")
        ));
        
        assertThat(result.jwtToken()).isEqualTo("jwt");
    }
}
