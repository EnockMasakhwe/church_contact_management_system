package com.eliarojr.contact_form.dto;

import com.eliarojr.contact_form.entity.enums.Role;
import lombok.Data;

@Data
public class AuthResponse {

    private String username;
    private String token;
    private Role role;
}
