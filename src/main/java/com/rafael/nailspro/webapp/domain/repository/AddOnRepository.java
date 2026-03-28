package com.rafael.nailspro.webapp.domain.repository;

import com.rafael.nailspro.webapp.domain.model.AppointmentAddOn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddOnRepository extends JpaRepository<AppointmentAddOn, Long> {
}
