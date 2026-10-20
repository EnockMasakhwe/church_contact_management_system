package com.eliarojr.contact_form.dto;

import com.eliarojr.contact_form.entity.enums.Role;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String username,
        String email,
        Role role,
        boolean enabled,
        LocalDateTime createdAt
) {
}
