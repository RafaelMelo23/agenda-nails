package com.rafael.agendanails.webapp.infrastructure.security.filter;

import com.rafael.agendanails.webapp.shared.tenant.TenantContext;
import com.rafael.agendanails.webapp.shared.tenant.TenantResolver;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class TenantIdFilter implements Filter {

    private final TenantResolver tenantResolver;

    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain
    ) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            String path = request.getRequestURI();

            boolean isPublicPath = path.equals("/")
                    || path.equals("/index.html")
                    || path.startsWith("/actuator")
                    || path.startsWith("/swagger-ui")
                    || path.startsWith("/v3/api-docs")
                    || path.startsWith("/api/v1/webhook")
                    || path.startsWith("/api/v1/auth")
                    || path.startsWith("/api/v1/notifications/subscribe")
                    || path.startsWith("/api/internal")
                    || path.startsWith("/public")
                    || path.startsWith("/pages")
                    || path.startsWith("/css")
                    || path.startsWith("/js")
                    || path.startsWith("/assets")
                    || path.startsWith("/favicon.svg")
                    || path.startsWith("/favicon.ico")
                    || path.startsWith("/error")
                    || path.startsWith("/uploads");

            if (isPublicPath) {
                filterChain.doFilter(request, response);
                return;
            }

            String tenantId = tenantResolver.resolve(request);

            if (tenantId != null) {
                MDC.put("tenant", tenantId);
                TenantContext.setTenant(tenantId);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant cannot be null");
                return;
            }

            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
            MDC.remove("tenant");
        }
    }
}