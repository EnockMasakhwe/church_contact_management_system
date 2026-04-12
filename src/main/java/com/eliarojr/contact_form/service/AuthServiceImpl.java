package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.AuthRequest;
import com.eliarojr.contact_form.dto.AuthResponse;
import com.eliarojr.contact_form.dto.RegisterRequest;
import com.eliarojr.contact_form.entity.Role;
import com.eliarojr.contact_form.entity.User;
import com.eliarojr.contact_form.entity.VerificationToken;
import com.eliarojr.contact_form.repository.UserRepository;
import com.eliarojr.contact_form.repository.VerificationTokenRepository;
import com.eliarojr.contact_form.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    @Override
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already exits!");
        }

        User user =  new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setEnabled(false);

        userRepository.save(user);

        //Generate token
        String token = UUID.randomUUID().toString();

        verificationTokenRepository.deleteByUser(user);

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .createdAt(LocalDateTime.now())
                .build();

        verificationTokenRepository.save(verificationToken);

        // 📧 Send email
        emailService.sendVerificationEmail(user.getEmail(), token);

        return ("User registered successfully. Please verify your email.");
    }

    public String verifyUser(String token) {

        VerificationToken verificationToken = verificationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);

        userRepository.save(user);

        // cleanup
        verificationTokenRepository.delete(verificationToken);

        return "Account verified successfully!";
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
                //.orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isEnabled()) {
            throw new RuntimeException("Please verify your email before logging in.");
        }

        //Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        //Generate JWT
        String token = jwtService.generateToken(user.getEmail());

        //Build response
        AuthResponse response = new AuthResponse();
        response.setUsername(user.getUsername());
        response.setToken(token);
        response.setRole(user.getRole());

        return response;
    }
}
