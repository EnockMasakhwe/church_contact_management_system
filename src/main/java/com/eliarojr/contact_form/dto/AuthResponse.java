package com.eliarojr.contact_form.dto;

import com.eliarojr.contact_form.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class AuthResponse {

    private String username;
    private String token;
    private Role role;
}
