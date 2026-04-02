package com.eliarojr.contact_form.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Wrong email format")
    private String email;
    @NotBlank(message = "Password is required")
    private String password;
}
