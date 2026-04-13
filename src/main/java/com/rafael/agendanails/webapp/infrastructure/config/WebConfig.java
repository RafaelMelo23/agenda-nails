package com.rafael.agendanails.webapp.infrastructure.config;

import com.rafael.agendanails.webapp.infrastructure.security.interceptor.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir}")
    private String uploadDir;

    private final SalonMaintenanceInterceptor salonMaintenanceInterceptor;
    private final TenantStatusInterceptor tenantStatusInterceptor;
    private final EvolutionApiInterceptor evolutionApiInterceptor;
    private final UserStatusInterceptor userStatusInterceptor;
    private final DemoReadOnlyInterceptor demoReadOnlyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(salonMaintenanceInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/v1/auth/**", "/api/v1/webhook/**", "/api/v1/admin/**", "/api/internal/**");

        registry.addInterceptor(tenantStatusInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/v1/auth/**", "/api/v1/webhook/**", "/api/v1/admin/**", "/api/internal/**");

        registry.addInterceptor(userStatusInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/api/v1/auth/**", "/api/v1/webhook/**", "/api/internal/**");

        registry.addInterceptor(evolutionApiInterceptor)
                .addPathPatterns("/api/v1/webhook", "/api/v1/webhook/**");

        registry.addInterceptor(demoReadOnlyInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/api/v1/auth/**", "/api/v1/webhook/**", "/api/internal/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");

        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(1, java.util.concurrent.TimeUnit.DAYS).cachePublic());
    }
}