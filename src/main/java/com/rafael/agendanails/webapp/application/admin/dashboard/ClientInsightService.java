package com.rafael.agendanails.webapp.application.admin.dashboard;

import com.rafael.agendanails.webapp.domain.repository.ClientAuditMetricsRepository;
import com.rafael.agendanails.webapp.infrastructure.dto.client.AdminClientCrmDTO;
import com.rafael.agendanails.webapp.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientInsightService {

    private final ClientAuditMetricsRepository repository;

    public AdminClientCrmDTO getClientsAuditedInfo(Long clientId) {

        return repository.findByClient_Id(clientId)
                .map(cam -> AdminClientCrmDTO.builder()
                        .clientId(cam.getClient().getId())
                        .name(cam.getClient().getFullName())
                        .phoneNumber(cam.getClient().getPhoneNumber())
                        .totalSpent(cam.getTotalSpent())
                        .completedAppointments(cam.getCompletedAppointmentsCount())
                        .canceledAppointments(cam.getCanceledAppointmentsCount())
                        .missedAppointments(cam.getMissedAppointmentsCount())
                        .lastVisitDate(cam.getLastVisitDate())
                        .build()
                )
                .orElseThrow(() -> new BusinessException("O cliente não foi encontrado, ou não tem métricas registradas."));
    }
}