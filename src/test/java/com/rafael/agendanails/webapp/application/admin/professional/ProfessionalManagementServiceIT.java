package com.rafael.agendanails.webapp.application.admin.professional;

import com.rafael.agendanails.webapp.domain.enums.user.UserStatus;
import com.rafael.agendanails.webapp.domain.model.Professional;
import com.rafael.agendanails.webapp.domain.model.SalonService;
import com.rafael.agendanails.webapp.infrastructure.dto.admin.professional.CreateProfessionalDTO;
import com.rafael.agendanails.webapp.shared.tenant.TenantContext;
import com.rafael.agendanails.webapp.support.BaseIntegrationTest;
import com.rafael.agendanails.webapp.support.factory.TestProfessionalFactory;
import com.rafael.agendanails.webapp.support.factory.TestSalonServiceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalManagementServiceIT extends BaseIntegrationTest {

    @Autowired
    private ProfessionalManagementService professionalManagementService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldCreateProfessional() {
        SalonService service1 = salonServiceRepository.save(TestSalonServiceFactory.builder().name("Service 1").build());
        SalonService service2 = salonServiceRepository.save(TestSalonServiceFactory.builder().name("Service 2").build());

        CreateProfessionalDTO dto = new CreateProfessionalDTO(
                null,
                "New Professional",
                "new.pro@test.com",
                List.of(service1.getId(), service2.getId())
        );

        professionalManagementService.createProfessional(dto, "tenant-test");

        Professional professional = professionalRepository.findByEmailIgnoreCase("new.pro@test.com").orElseThrow();
        assertThat(professional.getFullName()).isEqualTo("New Professional");
        assertThat(professional.getSalonServices()).hasSize(2);
        assertThat(passwordEncoder.matches("mudar123", professional.getPassword())).isTrue();
        assertThat(professional.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void shouldDeactivateProfessional() {
        Professional professional = professionalRepository.save(TestProfessionalFactory.builder().status(UserStatus.ACTIVE).isActive(true).build());

        professionalManagementService.deactivateProfessional(professional.getId());

        Professional updatedProfessional = professionalRepository.findById(professional.getId()).orElseThrow();
        assertThat(updatedProfessional.getIsActive()).isFalse();
    }

    @Test
    void shouldNotDeactivateProfessionalFromAnotherTenant() {
        String tenantA = "tenant-a";
        String tenantB = "tenant-b";

        TenantContext.setTenant(tenantA);
        Professional professionalA = professionalRepository.save(TestProfessionalFactory.builder().tenantId(tenantA).status(UserStatus.ACTIVE).isActive(true).build());

        TenantContext.setTenant(tenantB);
        professionalManagementService.deactivateProfessional(professionalA.getId());

        TenantContext.setTenant(tenantA);
        Professional updatedProfessional = professionalRepository.findById(professionalA.getId()).orElseThrow();
        assertThat(updatedProfessional.getIsActive()).isTrue();
    }
}
