package com.eliarojr.contact_form.repository;

import com.eliarojr.contact_form.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            LocalDateTime end,
            LocalDateTime start
    );
}
