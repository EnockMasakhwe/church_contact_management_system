package com.eliarojr.contact_form.controller;

import com.eliarojr.contact_form.dto.AppointmentResponse;
import com.eliarojr.contact_form.entity.Appointment;
import com.eliarojr.contact_form.entity.enums.AppointmentStatus;
import com.eliarojr.contact_form.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ADMIN ENDPOINT
    @GetMapping("/api/admin/appointments")
    public Page<AppointmentResponse> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by("startTime").ascending());
        return appointmentService.getAllAppointments(pageable);
    }

    // PUBLIC CALENDAR ENDPOINT
    @GetMapping("/api/appointments/public")
    public List<AppointmentResponse> getPublicAppointments() {
        return appointmentService.getPublicAppointments();
    }

    // UPDATE STATUS (ADMIN)
    @PutMapping("/api/admin/appointments/{id}/status")
    public AppointmentResponse updateStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status
    ) {
        return appointmentService.updateStatus(id, status);
    }
}