package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.AuthRequest;
import com.eliarojr.contact_form.dto.AuthResponse;
import com.eliarojr.contact_form.dto.RegisterRequest;

public interface AuthService {
    String register(RegisterRequest request);
    String verifyUser(String token);
    AuthResponse login(AuthRequest request);

    void createPasswordResetToken(String email);

    void resetPassword(String token, String newPassword);
}
