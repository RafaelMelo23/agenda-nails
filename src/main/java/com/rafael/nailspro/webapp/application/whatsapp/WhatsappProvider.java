package com.rafael.nailspro.webapp.application.whatsapp;

import java.util.Optional;

public interface WhatsappProvider {

    void createInstance(String tenantId);
    void deleteInstance(String instanceId);
    void instanceConnect(String instanceName, Optional<String> phoneNumber);
    void sendText(String tenantId, String message, String targetNumber);
    void logout(String instanceName);
}
