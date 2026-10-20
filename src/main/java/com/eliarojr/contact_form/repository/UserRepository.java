package com.eliarojr.contact_form.repository;

import com.eliarojr.contact_form.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    boolean existsByEmail( String email);
    User findByUsername(String username);
    List<User> findAllByOrderByCreatedAtDesc();
}
