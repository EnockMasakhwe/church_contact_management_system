package com.eliarojr.contact_form;

import com.eliarojr.contact_form.entity.PasswordResetToken;
import com.eliarojr.contact_form.entity.User;
import com.eliarojr.contact_form.entity.VerificationToken;
import com.eliarojr.contact_form.event.AuthEmailEvent;
import com.eliarojr.contact_form.repository.PasswordResetTokenRepository;
import com.eliarojr.contact_form.repository.UserRepository;
import com.eliarojr.contact_form.repository.VerificationTokenRepository;
import com.eliarojr.contact_form.service.JwtService;
import com.eliarojr.contact_form.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthRecoveryServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private VerificationTokenRepository verificationTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "superAdminEmail", "admin@example.com");
    }

    @Test
    void passwordResetForUnknownEmailDoesNothing() {
        when(userRepository.findByEmailForUpdate("unknown@example.com")).thenReturn(Optional.empty());

        authService.createPasswordResetToken(" Unknown@Example.com ");

        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void passwordResetRequestsAreThrottledAndUseNormalizedEmail() {
        User user = user("member@example.com");
        user.setPasswordResetRequestedAt(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmailForUpdate("member@example.com")).thenReturn(Optional.of(user));

        authService.createPasswordResetToken(" MEMBER@example.com ");

        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void passwordResetRequestCreatesTokenAfterCooldownAndPublishesEmail() {
        User user = user("member@example.com");
        user.setPasswordResetRequestedAt(LocalDateTime.now().minusMinutes(3));
        when(userRepository.findByEmailForUpdate("member@example.com")).thenReturn(Optional.of(user));

        authService.createPasswordResetToken("member@example.com");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        assertNotNull(tokenCaptor.getValue().getToken());
        assertEquals(user, tokenCaptor.getValue().getUser());
        assertNotNull(user.getPasswordResetRequestedAt());

        ArgumentCaptor<AuthEmailEvent> eventCaptor = ArgumentCaptor.forClass(AuthEmailEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(AuthEmailEvent.Type.PASSWORD_RESET, eventCaptor.getValue().type());
        assertEquals(user.getEmail(), eventCaptor.getValue().email());
    }

    @Test
    void verificationResendDoesNotRevealOrSendForUnknownAccount() {
        when(userRepository.findByEmailForUpdate("unknown@example.com")).thenReturn(Optional.empty());

        authService.resendVerificationEmail("unknown@example.com");

        verify(verificationTokenRepository, never()).save(any(VerificationToken.class));
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void verificationResendHonorsCooldown() {
        User user = user("member@example.com");
        VerificationToken token = VerificationToken.builder()
                .user(user)
                .createdAt(LocalDateTime.now().minusMinutes(1))
                .expiryDate(LocalDateTime.now().plusHours(23))
                .token("current-token")
                .build();
        when(userRepository.findByEmailForUpdate("member@example.com")).thenReturn(Optional.of(user));
        when(verificationTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        authService.resendVerificationEmail("member@example.com");

        verify(verificationTokenRepository, never()).save(any(VerificationToken.class));
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void verificationResendReplacesExpiredCooldownToken() {
        User user = user("member@example.com");
        VerificationToken token = VerificationToken.builder()
                .user(user)
                .createdAt(LocalDateTime.now().minusMinutes(3))
                .expiryDate(LocalDateTime.now().minusHours(1))
                .token("expired-token")
                .build();
        when(userRepository.findByEmailForUpdate("member@example.com")).thenReturn(Optional.of(user));
        when(verificationTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        authService.resendVerificationEmail("member@example.com");

        verify(verificationTokenRepository).delete(token);
        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(verificationTokenRepository).save(tokenCaptor.capture());
        assertNotNull(tokenCaptor.getValue().getToken());
        assertEquals(user, tokenCaptor.getValue().getUser());

        ArgumentCaptor<AuthEmailEvent> eventCaptor = ArgumentCaptor.forClass(AuthEmailEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(AuthEmailEvent.Type.VERIFICATION, eventCaptor.getValue().type());
        assertEquals(user.getEmail(), eventCaptor.getValue().email());
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setUsername("Member");
        user.setEnabled(false);
        return user;
    }
}
