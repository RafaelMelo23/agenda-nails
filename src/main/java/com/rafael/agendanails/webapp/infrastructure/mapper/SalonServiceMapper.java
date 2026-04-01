package com.rafael.agendanails.webapp.infrastructure.mapper;

import com.rafael.agendanails.webapp.domain.model.SalonService;
import com.rafael.agendanails.webapp.infrastructure.dto.salon.service.SalonServiceOutDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SalonServiceMapper {

    private final ProfessionalMapper professionalMapper;

    public List<SalonServiceOutDTO> mapToOutDTOList(List<SalonService> services) {
        return services.stream()
                .map(s -> SalonServiceOutDTO.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .value(s.getValue())
                        .durationInSeconds(s.getDurationInSeconds())
                        .description(s.getDescription())
                        .professionals(professionalMapper.mapProfessionalsToSimplifiedDTO(s.getProfessionals()))
                        .isActive(s.getActive())
                        .isAddOn(s.isAddOn())
                        .nailCount(s.getNailCount())
                        .build())
                .collect(Collectors.toList());
    }
}
