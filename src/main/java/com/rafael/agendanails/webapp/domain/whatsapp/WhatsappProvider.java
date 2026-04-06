package com.rafael.agendanails.webapp.domain.whatsapp;

public interface WhatsappProvider {

    void createInstance(String tenantId, String phoneNumber);
    void deleteInstance(String instanceId);
    String instanceConnect(String instanceName, String phoneNumber);
    void logout(String instanceName);

    SentMessageResult sendText(
            String tenantId,
            String message,
            String targetNumber
    );
}
