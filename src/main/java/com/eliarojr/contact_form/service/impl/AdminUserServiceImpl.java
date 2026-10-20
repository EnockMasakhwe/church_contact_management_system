package com.eliarojr.contact_form.service.impl;

import com.eliarojr.contact_form.dto.AdminUserResponse;
import com.eliarojr.contact_form.entity.User;
import com.eliarojr.contact_form.entity.enums.Role;
import com.eliarojr.contact_form.exception.BadRequestException;
import com.eliarojr.contact_form.exception.ResourceNotFoundException;
import com.eliarojr.contact_form.repository.MessageRepository;
import com.eliarojr.contact_form.repository.PasswordResetTokenRepository;
import com.eliarojr.contact_form.repository.UserRepository;
import com.eliarojr.contact_form.repository.VerificationTokenRepository;
import com.eliarojr.contact_form.service.AdminUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.super-admin-email}")
    private String superAdminEmail;

    @Override
    public List<AdminUserResponse> getUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AdminUserResponse updateRole(Long userId, Role role) {
        if (role != Role.ROLE_ADMIN && role != Role.ROLE_USER) {
            throw new BadRequestException("Role must be ROLE_ADMIN or ROLE_USER");
        }

        User user = findUser(userId);
        if (isSuperAdmin(user)) {
            throw new BadRequestException("The superadmin role cannot be changed");
        }

        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long userId) {
        User user = findUser(userId);
        if (isSuperAdmin(user)) {
            throw new BadRequestException("The superadmin account cannot be deleted");
        }

        verificationTokenRepository.deleteByUser(user);
        passwordResetTokenRepository.deleteByUser(user);
        messageRepository.deleteAll(messageRepository.findByUser_IdOrderByCreatedAtDesc(userId));
        userRepository.delete(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private boolean isSuperAdmin(User user) {
        return user.getEmail().equalsIgnoreCase(superAdminEmail);
    }

    private AdminUserResponse toResponse(User user) {
        Role effectiveRole = isSuperAdmin(user) ? Role.ROLE_SUPER_ADMIN : user.getRole();
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                effectiveRole,
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
