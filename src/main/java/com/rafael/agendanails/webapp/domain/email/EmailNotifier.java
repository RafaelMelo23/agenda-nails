package com.rafael.agendanails.webapp.domain.email;

public interface EmailNotifier {

    void send(EmailMessage emailMessage);
}