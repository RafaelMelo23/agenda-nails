package com.rafael.agendanails.webapp.shared.tenant;

import jakarta.servlet.ServletRequest;

public interface TenantResolver {

    String resolve(ServletRequest servletRequest);
}
