package com.eliarojr.contact_form.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        String message,
        String type,
        String status,
        String location,
        LocalDateTime preferredDateTime,
        String phone,
        String name,
        String email,
        LocalDateTime createdAt
) {}
