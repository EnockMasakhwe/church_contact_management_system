package com.eliarojr.contact_form.dto;

import com.eliarojr.contact_form.entity.enums.Role;

public record AuthResponse(
        String username,
        String token,
        Role role
) {}
