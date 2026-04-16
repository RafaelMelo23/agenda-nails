package com.rafael.agendanails.webapp.domain.model;

import com.rafael.agendanails.webapp.domain.enums.user.UserRole;
import com.rafael.agendanails.webapp.domain.enums.user.UserStatus;
import com.rafael.agendanails.webapp.infrastructure.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Client extends User {

    @Column(name = "missed_appointments")
    private Integer missedAppointments = 0;

    @Column(name = "cancelled_appointments")
    private Integer canceledAppointments = 0;

    @OneToMany(mappedBy = "client")
    private List<Appointment> clientAppointments;

    @Column(name = "phone_number", length = 13)
    private String phoneNumber;

    @OneToOne(mappedBy = "client", orphanRemoval = true)
    private ClientAuditMetrics clientAuditMetrics;

    @Override
    public void prePersist() {
        super.prePersist();

        if (this.getUserRole() == null) {
            setUserRole(UserRole.CLIENT);
        }

        if (this.missedAppointments == null) this.missedAppointments = 0;
        if (this.canceledAppointments == null) this.setCanceledAppointments(0);
    }

    public static String extractFirstName(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "cliente";

        return fullName.split("\\s+")[0];
    }

    public void incrementCancelledAppointmentCount() {
        this.canceledAppointments++;
    }

    public void validateCanBook() {
        if (!this.getUserRole().equals(UserRole.CLIENT)) {
            throw new BusinessException("Apenas clientes podem agendar");
        }
    }

    public static Client createDefault(String fullName, String email, String password) {
        return Client.builder()
                .fullName(fullName)
                .email(email)
                .password(password)
                .userRole(UserRole.CLIENT)
                .status(UserStatus.ACTIVE)
                .missedAppointments(0)
                .canceledAppointments(0)
                .phoneNumber(null)
                .build();
    }
}