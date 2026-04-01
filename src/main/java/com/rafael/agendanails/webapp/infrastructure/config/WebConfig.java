package com.rafael.agendanails.webapp.infrastructure.config;

import com.rafael.agendanails.webapp.infrastructure.security.interceptor.EvolutionApiInterceptor;
import com.rafael.agendanails.webapp.infrastructure.security.interceptor.SalonMaintenanceInterceptor;
import com.rafael.agendanails.webapp.infrastructure.security.interceptor.TenantStatusInterceptor;
import com.rafael.agendanails.webapp.infrastructure.security.interceptor.UserStatusInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SalonMaintenanceInterceptor salonMaintenanceInterceptor;
    private final TenantStatusInterceptor tenantStatusInterceptor;
    private final EvolutionApiInterceptor evolutionApiInterceptor;
    private final UserStatusInterceptor userStatusInterceptor;

    //todo: currently disabled for dev
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //    registry.addInterceptor(salonMaintenanceInterceptor);
        //    registry.addInterceptor(tenantStatusFilter);
        registry.addInterceptor(userStatusInterceptor)
                        .excludePathPatterns("/api/v1/auth/**");
        registry.addInterceptor(evolutionApiInterceptor)
                .addPathPatterns("/api/v1/webhook/**");
    }
}
