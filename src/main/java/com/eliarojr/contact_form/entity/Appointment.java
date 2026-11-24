package com.eliarojr.contact_form.entity;

import com.eliarojr.contact_form.entity.enums.AppointmentStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_appointment_reserved_slot",
        columnNames = "reserved_slot"
))
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(name = "reserved_slot")
    private LocalDateTime reservedSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;
}
