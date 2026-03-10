package com.rafael.nailspro.webapp.application.internal;

import com.rafael.nailspro.webapp.domain.enums.appointment.TenantStatus;
import com.rafael.nailspro.webapp.domain.model.SalonProfile;
import com.rafael.nailspro.webapp.domain.repository.SalonProfileRepository;
import com.rafael.nailspro.webapp.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantManagementService {

    private final SalonProfileRepository salonProfileRepository;

    @Transactional
    public boolean updateTenantStatus(String tenantId, TenantStatus status) {
        return salonProfileRepository.findByTenantId(tenantId)
                .map(salon -> {
                    salon.setTenantStatus(status);
                    salonProfileRepository.save(salon);
                    return true;
                })
                .orElseThrow(() -> new BusinessException("Tenant não encontrado."));
    }
}