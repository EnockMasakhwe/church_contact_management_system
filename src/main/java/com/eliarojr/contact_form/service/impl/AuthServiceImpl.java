package com.eliarojr.contact_form.service.impl;

import com.eliarojr.contact_form.dto.AuthRequest;
import com.eliarojr.contact_form.dto.AuthResponse;
import com.eliarojr.contact_form.dto.RegisterRequest;
import com.eliarojr.contact_form.entity.PasswordResetToken;
import com.eliarojr.contact_form.entity.enums.Role;
import com.eliarojr.contact_form.entity.User;
import com.eliarojr.contact_form.entity.VerificationToken;
import com.eliarojr.contact_form.event.AuthEmailEvent;
import com.eliarojr.contact_form.exception.BadRequestException;
import com.eliarojr.contact_form.exception.UnauthorizedException;
import com.eliarojr.contact_form.repository.PasswordResetTokenRepository;
import com.eliarojr.contact_form.repository.UserRepository;
import com.eliarojr.contact_form.repository.VerificationTokenRepository;
import com.eliarojr.contact_form.service.JwtService;
import com.eliarojr.contact_form.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.super-admin-email}")
    private String superAdminEmail;

    @Override
    public String register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)){
            throw new BadRequestException("Email already exists");
        }

        User user =  new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(email);
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

        eventPublisher.publishEvent(new AuthEmailEvent(
                user.getEmail(),
                token,
                AuthEmailEvent.Type.VERIFICATION
        ));

        return ("User registered successfully. Please verify your email.");
    }

    @Override
    public String verifyUser(String token) {

        VerificationToken verificationToken = verificationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expired");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);

        userRepository.save(user);

        // cleanup
        verificationTokenRepository.delete(verificationToken);

        return "Account verified successfully!";
    }

    @Override
    public void resendVerificationEmail(String email) {
        Optional<User> optionalUser = userRepository.findByEmailForUpdate(normalizeEmail(email));
        if (optionalUser.isEmpty() || optionalUser.get().isEnabled()) {
            return;
        }

        User user = optionalUser.get();
        LocalDateTime now = LocalDateTime.now();
        Optional<VerificationToken> existing = verificationTokenRepository.findByUser(user);
        if (existing.isPresent()) {
            VerificationToken token = existing.get();
            if (token.getCreatedAt() != null && token.getCreatedAt().isAfter(now.minusMinutes(2))) {
                return;
            }
            verificationTokenRepository.delete(token);
        }

        String token = UUID.randomUUID().toString();
        verificationTokenRepository.save(VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(now.plusHours(24))
                .createdAt(now)
                .build());
        eventPublisher.publishEvent(new AuthEmailEvent(
                user.getEmail(),
                token,
                AuthEmailEvent.Type.VERIFICATION
        ));
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Please verify your email before logging in");
        }

        //Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password!");
        }

        //Generate JWT
        String token = jwtService.generateToken(user.getEmail());

        //Build response
        return new AuthResponse(
                user.getUsername(),
                token,
                user.getEmail().equalsIgnoreCase(superAdminEmail)
                        ? Role.ROLE_SUPER_ADMIN
                        : user.getRole()
        );
    }

    @Override
    public void createPasswordResetToken(String email) {

        Optional<User> optionalUser = userRepository.findByEmailForUpdate(normalizeEmail(email));
        if (optionalUser.isEmpty()) {
            return;
        }
        User user = optionalUser.get();
        LocalDateTime now = LocalDateTime.now();
        if (user.getPasswordResetRequestedAt() != null
                && user.getPasswordResetRequestedAt().isAfter(now.minusMinutes(2))) {
            return;
        }

        String token = UUID.randomUUID().toString();

        passwordResetTokenRepository.deleteByUser(user);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(now.plusMinutes(30));
        resetToken.setCreatedAt(now);

        passwordResetTokenRepository.save(resetToken);
        user.setPasswordResetRequestedAt(now);
        userRepository.save(user);

        eventPublisher.publishEvent(new AuthEmailEvent(
                user.getEmail(),
                token,
                AuthEmailEvent.Type.PASSWORD_RESET
        ));
    }

    @Override
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetRequestedAt(null);

        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
