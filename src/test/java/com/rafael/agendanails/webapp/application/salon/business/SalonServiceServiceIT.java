package com.rafael.agendanails.webapp.application.salon.business;

import com.rafael.agendanails.webapp.domain.model.Professional;
import com.rafael.agendanails.webapp.domain.model.SalonService;
import com.rafael.agendanails.webapp.infrastructure.dto.salon.service.SalonServiceDTO;
import com.rafael.agendanails.webapp.infrastructure.dto.salon.service.SalonServiceOutDTO;
import com.rafael.agendanails.webapp.shared.tenant.TenantContext;
import com.rafael.agendanails.webapp.support.BaseIntegrationTest;
import com.rafael.agendanails.webapp.support.factory.TestProfessionalFactory;
import com.rafael.agendanails.webapp.support.factory.TestSalonServiceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SalonServiceServiceIT extends BaseIntegrationTest {

    @Autowired
    private SalonServiceService salonServiceService;

    @Test
    void shouldCreateService() {
        Professional pro = professionalRepository.save(TestProfessionalFactory.builder().build());

        SalonServiceDTO dto = SalonServiceDTO.builder()
                .name("New Service")
                .description("New Description")
                .value(100)
                .durationInSeconds(3600)
                .professionalsIds(List.of(pro.getId()))
                .build();

        salonServiceService.createService(dto);

        List<SalonServiceOutDTO> services = salonServiceService.getServices();
        assertThat(services).hasSize(1);
        assertThat(services.get(0).name()).isEqualTo("New Service");
    }

    @Test
    void shouldUpdateService() {
        SalonService service = salonServiceRepository.save(TestSalonServiceFactory.builder().build());
        
        SalonServiceDTO dto = SalonServiceDTO.builder()
                .name("Updated Name")
                .value(150)
                .build();

        salonServiceService.updateSalonService(service.getId(), dto);

        SalonService updatedService = salonServiceRepository.findById(service.getId()).orElseThrow();
        assertThat(updatedService.getName()).isEqualTo("Updated Name");
        assertThat(updatedService.getValue()).isEqualTo(150);
    }

    @Test
    void shouldChangeVisibility() {
        SalonService service = salonServiceRepository.save(TestSalonServiceFactory.builder().active(true).build());

        salonServiceService.changeSalonServiceVisibility(service.getId(), false);

        SalonService updatedService = salonServiceRepository.findById(service.getId()).orElseThrow();
        assertThat(updatedService.getActive()).isFalse();
    }

    @Test
    void shouldRespectTenantIsolation() {
        String tenantA = "tenant-a";
        String tenantB = "tenant-b";

        TenantContext.setTenant(tenantA);
        salonServiceRepository.save(TestSalonServiceFactory.builder().tenantId(tenantA).name("Service A").active(true).build());

        TenantContext.setTenant(tenantB);
        salonServiceRepository.save(TestSalonServiceFactory.builder().tenantId(tenantB).name("Service B").active(true).build());

        TenantContext.setTenant(tenantA);
        List<SalonServiceOutDTO> servicesA = salonServiceService.getServices();

        assertThat(servicesA).hasSize(1);
        assertThat(servicesA.get(0).name()).isEqualTo("Service A");
    }
}
