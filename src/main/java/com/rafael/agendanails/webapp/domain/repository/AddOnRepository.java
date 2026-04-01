package com.rafael.agendanails.webapp.domain.repository;

import com.rafael.agendanails.webapp.domain.model.AppointmentAddOn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddOnRepository extends JpaRepository<AppointmentAddOn, Long> {
}
