package com.eliarojr.contact_form.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @Column(nullable = false)
    private String token;
    @Column(nullable = false)
    private String newPassword;
}
