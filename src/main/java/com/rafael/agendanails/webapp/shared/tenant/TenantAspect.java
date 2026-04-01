package com.rafael.agendanails.webapp.shared.tenant;

import com.rafael.agendanails.webapp.infrastructure.exception.TenantNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.Session;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class TenantAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Around("execution(* com.rafael.agendanails.webapp..*(..))")
    @Order(1)
    public Object handleTenancy(ProceedingJoinPoint pjp) throws Throwable {
        boolean hasIgnoreAnnotation = hasIgnoreAnnotation(pjp);
        boolean isRepo = isRepositoryCall(pjp);

        if (hasIgnoreAnnotation) {
            boolean previous = TenantContext.isIgnoreFilter();
            TenantContext.setIgnoreFilter(true);
            try {
                if (isRepo) {
                    disableFilter();
                }
                return pjp.proceed();
            } finally {
                TenantContext.setIgnoreFilter(previous);
            }
        }

        if (isRepo) {
            if (TenantContext.isIgnoreFilter()) {
                disableFilter();
            } else {
                enableFilter();
            }
        }

        return pjp.proceed();
    }

    private void disableFilter() {
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("tenantFilter");
        log.trace("TenantAspect: [FILTER-OFF] disabled for session");
    }

    private void enableFilter() {
        String tenantId = TenantContext.getTenant();
        if (tenantId == null) {
            log.error("TenantAspect: [DENIED] access without tenant");
            throw new TenantNotFoundException();
        }

        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        log.trace("TenantAspect: [FILTER-ON] enabled for tenant: [{}]", tenantId);
    }

    private boolean isRepositoryCall(ProceedingJoinPoint pjp) {
        String declaringType = pjp.getSignature().getDeclaringTypeName();
        return declaringType.contains(".domain.repository.") || 
               pjp.getTarget() instanceof org.springframework.data.repository.Repository;
    }

    private boolean hasIgnoreAnnotation(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        return AnnotationUtils.findAnnotation(method, IgnoreTenantFilter.class) != null ||
               AnnotationUtils.findAnnotation(method.getDeclaringClass(), IgnoreTenantFilter.class) != null ||
               AnnotationUtils.findAnnotation(pjp.getTarget().getClass(), IgnoreTenantFilter.class) != null;
    }
}