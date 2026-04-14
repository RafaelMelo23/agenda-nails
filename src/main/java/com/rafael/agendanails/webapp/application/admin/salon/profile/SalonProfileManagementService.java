package com.rafael.agendanails.webapp.application.admin.salon.profile;

import com.rafael.agendanails.webapp.domain.model.SalonProfile;
import com.rafael.agendanails.webapp.domain.repository.SalonProfileRepository;
import com.rafael.agendanails.webapp.infrastructure.config.CacheConfig;
import com.rafael.agendanails.webapp.infrastructure.dto.admin.salon.profile.SalonProfileDTO;
import com.rafael.agendanails.webapp.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class SalonProfileManagementService {

    private final SalonProfileRepository repository;

    @Transactional(readOnly = true)
    public SalonProfileDTO getProfile(String tenantId) {
        SalonProfile salonProfile = repository.findByTenantId(tenantId)
                .orElseThrow(() -> new BusinessException("O perfil do salão não foi encontrado."));

        return SalonProfileDTO.builder()
                .tradeName(salonProfile.getTradeName())
                .slogan(salonProfile.getSlogan())
                .primaryColor(salonProfile.getPrimaryColor())
                .comercialPhone(salonProfile.getComercialPhone())
                .fullAddress(salonProfile.getFullAddress())
                .socialMediaLink(salonProfile.getSocialMediaLink())
                .status(salonProfile.getOperationalStatus())
                .warningMessage(salonProfile.getWarningMessage())
                .appointmentBufferMinutes(salonProfile.getAppointmentBufferMinutes())
                .zoneId(salonProfile.getZoneId())
                .isLoyalClientelePrioritized(salonProfile.isLoyalClientelePrioritized())
                .loyalClientBookingWindowDays(salonProfile.getLoyalClientBookingWindowDays())
                .standardBookingWindow(salonProfile.getStandardBookingWindow())
                .connectionState(salonProfile.getEvolutionConnectionState())
                .autoConfirmationAppointment(salonProfile.isAutoConfirmationAppointment())
                .build();
    }

    @CacheEvict(value = CacheConfig.SALON_PROFILE_CACHE, key = "#tenantId")
    @Transactional
    public void updateProfile(String tenantId, SalonProfileDTO profile) {
        SalonProfile salonProfile = repository.findByTenantId(tenantId)
                .orElseThrow(() -> new BusinessException("O perfil do salão não foi encontrado."));

        setIfNotNull(profile.tradeName(), salonProfile::setTradeName);
        setIfNotNull(profile.slogan(), salonProfile::setSlogan);
        setIfNotNull(profile.primaryColor(), salonProfile::setPrimaryColor);
        setIfNotNull(profile.comercialPhone(), salonProfile::setComercialPhone);
        setIfNotNull(profile.fullAddress(), salonProfile::setFullAddress);
        setIfNotNull(profile.socialMediaLink(), salonProfile::setSocialMediaLink);
        setIfNotNull(profile.warningMessage(), salonProfile::setWarningMessage);
        setIfNotNull(profile.status(), salonProfile::setOperationalStatus);
        setIfNotNull(profile.appointmentBufferMinutes(), salonProfile::setAppointmentBufferMinutes);
        setIfNotNull(profile.zoneId(), salonProfile::setZoneId);
        setIfNotNull(profile.isLoyalClientelePrioritized(), salonProfile::setLoyalClientelePrioritized);
        setIfNotNull(profile.loyalClientBookingWindowDays(), salonProfile::setLoyalClientBookingWindowDays);
        setIfNotNull(profile.standardBookingWindow(), salonProfile::setStandardBookingWindow);
        setIfNotNull(profile.autoConfirmationAppointment(), salonProfile::setAutoConfirmationAppointment);

        validateLoyalClientFeature(profile);

        repository.save(salonProfile);
    }

    private static void validateLoyalClientFeature(SalonProfileDTO profile) {
        if (Boolean.TRUE.equals(profile.isLoyalClientelePrioritized()) && (
                profile.loyalClientBookingWindowDays() == null ||
                profile.standardBookingWindow() == null)) {
            throw new BusinessException("""
                    O número de dias de antecedência
                    para clientes fiéis deve ser informado
                    quando a priorização de clientes fiéis estiver ativada.""");
        }
    }

    public <T> void setIfNotNull(T value, Consumer<T> setter) {
        if (value != null) setter.accept(value);
    }
}