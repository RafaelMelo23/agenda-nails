package com.rafael.agendanails.webapp.domain.whatsapp;

public interface WhatsappProvider {

    void createInstance(String tenantId);
    void deleteInstance(String instanceId);
    void instanceConnect(String instanceName, String phoneNumber);
    void logout(String instanceName);

    SentMessageResult sendText(
            String tenantId,
            String message,
            String targetNumber
    );
}
