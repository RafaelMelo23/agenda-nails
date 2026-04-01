package com.rafael.agendanails.webapp.infrastructure.dto.whatsapp.evolution;

import java.util.List;

public class EvolutionErrorResponse {

    private int status;
    private String error;
    private EvolutionErrorDetails response;

    public static class EvolutionErrorDetails {
        private List<String> message;
    }
}