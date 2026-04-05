package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.AuthRequest;
import com.eliarojr.contact_form.dto.AuthResponse;
import com.eliarojr.contact_form.dto.RegisterRequest;

public interface AuthService {
    String register(RegisterRequest request);

    AuthResponse login(AuthRequest request);
}
