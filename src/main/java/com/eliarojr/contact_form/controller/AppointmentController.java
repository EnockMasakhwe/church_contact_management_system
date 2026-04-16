package com.eliarojr.contact_form.controller;

import com.eliarojr.contact_form.dto.AppointmentResponse;
import com.eliarojr.contact_form.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public List<AppointmentResponse> getCalendar() {
        return appointmentService.getAllAppointments();
    }
}
