package com.rafael.nailspro.webapp.infrastructure.mapper;

import com.rafael.nailspro.webapp.domain.model.SalonService;
import com.rafael.nailspro.webapp.infrastructure.dto.salon.service.SalonServiceOutDTO;
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
                        .build())
                .collect(Collectors.toList());
    }
}
