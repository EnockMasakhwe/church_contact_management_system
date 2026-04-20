package com.eliarojr.contact_form.entity;

import com.eliarojr.contact_form.entity.enums.AppointmentStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "message_id")
    private ContactMessage message;
}
