package com.rafael.agendanails.webapp.infrastructure.config;

import io.sentry.Sentry;
import io.sentry.SentryEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;

@Configuration
public class SentryConfig {

    @Bean
    public Sentry.OptionsConfiguration sentryOptionsConfiguration() {
        return options -> {
            options.setBeforeSend((event, hint) -> {
                if (event.getThrowable() instanceof AccessDeniedException) {
                    return null;
                }

                if (isHttpStatusCode(event, 404)) {
                    return null;
                }

                return event;
            });
        };
    }

    public boolean isHttpStatusCode(SentryEvent event, int statusCode) {
        return event.getContexts().getResponse() != null
                && event.getContexts().getResponse().getStatusCode() != null
                && event.getContexts().getResponse().getStatusCode() == statusCode;
    }
}