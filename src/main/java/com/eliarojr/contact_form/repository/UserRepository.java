package com.eliarojr.contact_form.repository;

import com.eliarojr.contact_form.entity.User;
import com.eliarojr.contact_form.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    boolean existsByEmail( String email);
    User findByUsername(String username);
    List<User> findAllByOrderByCreatedAtDesc();

    @Query("""
            SELECT u FROM User u
            WHERE u.role IN :roles OR LOWER(u.email) = LOWER(:superAdminEmail)
            """)
    List<User> findAdminRecipients(
            @Param("roles") List<Role> roles,
            @Param("superAdminEmail") String superAdminEmail
    );
}
