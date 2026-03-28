package com.rafael.nailspro.webapp.application.messages;

import com.rafael.nailspro.webapp.domain.model.Appointment;
import com.rafael.nailspro.webapp.domain.model.RetentionForecast;
import com.rafael.nailspro.webapp.domain.model.SalonService;
import com.rafael.nailspro.webapp.infrastructure.helper.TenantUrlProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
public class RetentionMessageBuilder {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");
    private final TenantUrlProvider urlProvider;

    public String buildRetentionMessage(RetentionForecast retentionForecast) {
        Appointment originAppointment = retentionForecast.getOriginAppointment();
        List<SalonService> salonServices = retentionForecast.getSalonServices();

        Instant expectedReturn = retentionForecast.getPredictedReturnDate();
        ZonedDateTime appointmentTime = ZonedDateTime.ofInstant(expectedReturn, originAppointment.getSalonZoneId());

        String servicesNames = salonServices.stream()
                .map(SalonService::getName)
                .collect(Collectors.joining(", "));

        String serviceLabel = salonServices.size() > 1 ? "dos seus serviços" : "do seu serviço";

        return String.format("""
                Olá, %s!
                Notamos que a data de manutenção %s está se aproximando.
                📅 Sugestão: %s
                💅 %s: %s
                Como nossa agenda costuma lotar rápido,
                liberei o link para você garantir sua vaga com antecedência: %s""",
                originAppointment.getClient().getFullName(),
                serviceLabel,
                appointmentTime.format(DATE_FORMATTER),
                salonServices.size() > 1 ? "Serviços" : "Serviço",
                servicesNames,
                urlProvider.buildBookAppointmentUrl(originAppointment.getTenantId())
        );
    }
}