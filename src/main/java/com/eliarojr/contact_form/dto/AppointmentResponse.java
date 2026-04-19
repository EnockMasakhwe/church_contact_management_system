package com.eliarojr.contact_form.dto;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,

        String name,
        String email,
        String message
) {}
