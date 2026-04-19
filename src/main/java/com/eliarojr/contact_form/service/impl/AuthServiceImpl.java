package com.eliarojr.contact_form.service.impl;

import com.eliarojr.contact_form.dto.AuthRequest;
import com.eliarojr.contact_form.dto.AuthResponse;
import com.eliarojr.contact_form.dto.RegisterRequest;
import com.eliarojr.contact_form.entity.PasswordResetToken;
import com.eliarojr.contact_form.entity.enums.Role;
import com.eliarojr.contact_form.entity.User;
import com.eliarojr.contact_form.entity.VerificationToken;
import com.eliarojr.contact_form.repository.PasswordResetTokenRepository;
import com.eliarojr.contact_form.repository.UserRepository;
import com.eliarojr.contact_form.repository.VerificationTokenRepository;
import com.eliarojr.contact_form.security.JwtService;
import com.eliarojr.contact_form.service.AuthService;
import com.eliarojr.contact_form.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

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

    @Override
    public void createPasswordResetToken(String email) {

        User user = userRepository.findByEmail(email);
               // .orElseThrow(() -> new RuntimeException("User not found"));//

        String token = UUID.randomUUID().toString();

        passwordResetTokenRepository.deleteByUser(user);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));

        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Override
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }
}
