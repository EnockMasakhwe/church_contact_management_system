package com.eliarojr.contact_form.controller;

import com.eliarojr.contact_form.dto.AppointmentResponse;
import com.eliarojr.contact_form.entity.Appointment;
import com.eliarojr.contact_form.entity.enums.AppointmentStatus;
import com.eliarojr.contact_form.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ADMIN ENDPOINT
    @GetMapping("/api/admin/appointments")
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    // PUBLIC CALENDAR ENDPOINT
    @GetMapping("/api/appointments/public")
    public List<AppointmentResponse> getPublicAppointments() {
        return appointmentService.getPublicAppointments();
    }

    // UPDATE STATUS (ADMIN)
    @PutMapping("/api/admin/appointments/{id}/status")
    public Appointment updateStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status
    ) {
        return appointmentService.updateStatus(id, status);
    }
}