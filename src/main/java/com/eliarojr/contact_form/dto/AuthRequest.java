package com.eliarojr.contact_form.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Wrong email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6)
    private String password;
}
