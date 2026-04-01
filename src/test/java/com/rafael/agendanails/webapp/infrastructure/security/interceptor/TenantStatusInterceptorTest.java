package com.rafael.agendanails.webapp.infrastructure.security.interceptor;

import com.rafael.agendanails.webapp.application.salon.business.SalonProfileService;
import com.rafael.agendanails.webapp.domain.enums.appointment.TenantStatus;
import com.rafael.agendanails.webapp.infrastructure.security.RequestPolicyManager;
import com.rafael.agendanails.webapp.shared.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantStatusInterceptorTest {

    @Mock
    private SalonProfileService salonProfileService;

    @Mock
    private RequestPolicyManager requestPolicyManager;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private TenantStatusInterceptor interceptor;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldReturnTrueWhenTenantIsActive() throws Exception {
        String tenantId = "active-tenant";
        TenantContext.setTenant(tenantId);
        when(request.getRequestURI()).thenReturn("/api/appointments");
        when(requestPolicyManager.isWhiteListed("/api/appointments")).thenReturn(false);
        when(salonProfileService.getStatusByTenantId(tenantId)).thenReturn(TenantStatus.ACTIVE);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void shouldReturnFalseAndSet402WhenTenantIsSuspended() throws Exception {
        String tenantId = "suspended-tenant";
        TenantContext.setTenant(tenantId);
        when(request.getRequestURI()).thenReturn("/api/appointments");
        when(requestPolicyManager.isWhiteListed("/api/appointments")).thenReturn(false);
        when(salonProfileService.getStatusByTenantId(tenantId)).thenReturn(TenantStatus.SUSPENDED);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isFalse();
        verify(response).setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
        verify(response).setContentType("application/json");
        assertThat(stringWriter.toString()).contains("Subscription suspended");
    }

    @Test
    void shouldReturnTrueEvenIfTenantSuspendedWhenPathIsWhiteListed() throws Exception {
        String tenantId = "suspended-tenant";
        TenantContext.setTenant(tenantId);
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(requestPolicyManager.isWhiteListed("/api/auth/login")).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verify(salonProfileService, never()).getStatusByTenantId(anyString());
    }

    @Test
    void shouldReturnTrueWithoutCheckingStatusWhenNoTenantAndNotWhiteListed() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/admin");
        when(requestPolicyManager.isWhiteListed("/api/admin")).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verify(salonProfileService, never()).getStatusByTenantId(any());
    }
}
