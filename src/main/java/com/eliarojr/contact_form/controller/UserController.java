package com.eliarojr.contact_form.controller;

import com.eliarojr.contact_form.dto.AppointmentResponse;
import com.eliarojr.contact_form.dto.MessageResponse;
import com.eliarojr.contact_form.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/messages")
    public List<MessageResponse> getMyMessages() {
        return userService.getMyMessages();
    }

    @GetMapping("/appointments")
    public List<AppointmentResponse> getMyAppointments() {
        return userService.getMyAppointments();
    }
}