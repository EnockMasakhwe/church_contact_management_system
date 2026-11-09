package com.eliarojr.contact_form.repository;

import com.eliarojr.contact_form.entity.Appointment;
import com.eliarojr.contact_form.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    //for booking (conflict detection)
    @Query("""
    SELECT a FROM Appointment a
    WHERE a.startTime < :end
      AND a.endTime > :start
      AND a.status IN :statuses
""")
    List<Appointment> findConflictingAppointments(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statuses") Collection<AppointmentStatus> statuses
    );

    // for calendar display
    List<Appointment> findAllByOrderByStartTimeAsc();

    //For user messages view
    List<Appointment> findByMessage_User_IdOrderByStartTimeAsc(Long userId);
    Long countByStatus(AppointmentStatus status);
}
