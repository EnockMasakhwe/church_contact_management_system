package com.eliarojr.contact_form.controller;

import com.eliarojr.contact_form.dto.AppointmentResponse;
import com.eliarojr.contact_form.dto.MessageResponse;
import com.eliarojr.contact_form.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/messages")
    public Page<MessageResponse> getMyMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userService.getMyMessages(pageable);
    }

    @GetMapping("/appointments")
    public Page<AppointmentResponse> getMyAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        return userService.getMyAppointments(pageable);
    }

    @PutMapping("/appointments/{id}/cancel")
    public AppointmentResponse cancelAppointment(@PathVariable Long id) {
        return userService.cancelMyAppointment(id);
    }
}