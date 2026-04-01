package com.rafael.agendanails.webapp.domain.email;

public interface EmailQuotaManager {
    boolean isQuotaAvailable();
    void registerSuccessfulSend();
}
