package com.eliarojr.contact_form.repository;

import com.eliarojr.contact_form.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    //for booking (conflict detection)
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.startTime < :end AND a.endTime > :start
    """)
    List<Appointment> findConflictingAppointments(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // for calendar display
    List<Appointment> findAllByOrderByStartTimeAsc();
}
