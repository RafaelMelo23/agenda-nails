package com.rafael.nailspro.webapp.shared.tenant;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.rafael.nailspro.webapp.infrastructure.security.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.hibernate.context.TenantIdentifierMismatchException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HeaderOrSubdomainTenantResolver implements TenantResolver {

    private final TokenService tokenService;

    @Override
    public String resolve(HttpServletRequest request) {
        DecodedJWT token = tokenService.recoverAndValidate(request);

        String tenantFromToken = (token != null)
                ? token.getClaim("tenantId").asString()
                : null;

        String host = request.getHeader("X-Forwarded-Host");
        if (host == null) request.getHeader("Host");
        if (host == null) request.getServerName();

        String tenantFromSubdomain = host.split("\\.")[0];

        if (tenantFromToken != null &&
                !tenantFromToken.equalsIgnoreCase(host)) {
            throw new TenantIdentifierMismatchException("Tenant mismatch between token and domain");
        }

        return (tenantFromToken != null)
                ? tenantFromToken
                : tenantFromSubdomain;
    }
}