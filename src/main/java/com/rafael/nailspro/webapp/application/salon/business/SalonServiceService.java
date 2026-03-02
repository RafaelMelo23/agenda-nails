package com.rafael.nailspro.webapp.application.salon.business;

import com.rafael.nailspro.webapp.domain.model.AppointmentAddOn;
import com.rafael.nailspro.webapp.domain.model.Professional;
import com.rafael.nailspro.webapp.domain.model.SalonService;
import com.rafael.nailspro.webapp.domain.repository.AddOnRepository;
import com.rafael.nailspro.webapp.domain.repository.ProfessionalRepository;
import com.rafael.nailspro.webapp.domain.repository.SalonServiceRepository;
import com.rafael.nailspro.webapp.infrastructure.dto.salon.service.SalonServiceDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.salon.service.SalonServiceOutDTO;
import com.rafael.nailspro.webapp.infrastructure.mapper.SalonServiceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalonServiceService {

    private final SalonServiceRepository salonServiceRepository;
    private final ProfessionalRepository professionalRepository;
    private final SalonServiceMapper salonServiceMapper;
    private final AddOnRepository addOnRepository;

    public SalonService findById(Long id) {

        return salonServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalonService not found"));
    }

    public Set<SalonService> findByIdIn(List<Long> ids) {

        return salonServiceRepository.findByIdIn(ids)
                .orElseThrow(() -> new RuntimeException("SalonService not found"));
    }

    public List<AppointmentAddOn> findAddOns(List<Long> addOnIds) {

        return addOnRepository.findAllById(addOnIds);
    }
    @Transactional
    public void createService(SalonServiceDTO salonServiceDTO) {

        Set<Professional> professionalsThatOfferSuchService = getProfessionals(salonServiceDTO);

        SalonService builtService = SalonService.create(salonServiceDTO, professionalsThatOfferSuchService);
        salonServiceRepository.save(builtService);
    }

    private Set<Professional> getProfessionals(SalonServiceDTO salonServiceDTO) {
        return salonServiceDTO.professionals()
                .filter(ids -> !ids.isEmpty())
                .map(ids -> new HashSet<>(professionalRepository.findAllById(ids)))
                .orElse(new HashSet<>());
    }

    public List<SalonServiceOutDTO> getServices() {
        List<SalonService> services = salonServiceRepository.findAll();
        return salonServiceMapper.mapToOutDTOList(services);
    }

    @Transactional
    public void changeSalonServiceVisibility(Long id, Boolean active) {

        salonServiceRepository.changeSalonServiceVisibility(id, active);
    }

    @Transactional
    public void deleteSalonService(Long id) {

        salonServiceRepository.deleteService(id);
    }

    @Transactional
    public void updateSalonService(Long serviceId, SalonServiceDTO dto) {
        SalonService service = findById(serviceId);

        if (dto.name() != null) {
            service.setName(dto.name());
        }

        if (dto.description() != null) {
            service.setDescription(dto.description());
        }

        if (dto.durationInSeconds() != null) {
            service.setDurationInSeconds(dto.durationInSeconds());
        }

        if (dto.value() != null) {
            service.setValue(dto.value());
        }

        dto.professionals().ifPresent(newIds -> {

            service.getProfessionals().removeIf(p -> !newIds.contains(p.getId()));

            Set<Long> currentIds = service.getProfessionals().stream()
                    .map(Professional::getId)
                    .collect(Collectors.toSet());

            List<Long> idsToAdd = newIds.stream()
                    .filter(id -> !currentIds.contains(id))
                    .toList();

            if (!idsToAdd.isEmpty()) {
                Set<Professional> professionalsToAdd = new HashSet<>(professionalRepository.findAllById(idsToAdd));
                service.getProfessionals().addAll(professionalsToAdd);
            }
        });
    }
}
