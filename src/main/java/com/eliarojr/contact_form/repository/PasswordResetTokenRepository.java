package com.eliarojr.contact_form.repository;

import com.eliarojr.contact_form.entity.PasswordResetToken;
import com.eliarojr.contact_form.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}
