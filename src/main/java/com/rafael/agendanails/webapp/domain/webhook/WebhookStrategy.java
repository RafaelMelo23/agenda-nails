package com.rafael.agendanails.webapp.domain.webhook;

public interface WebhookStrategy {

    void process(Object payload);

    String getSupportedTypeEvent();
}
