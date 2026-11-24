package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.AdminUserResponse;
import com.eliarojr.contact_form.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {
    Page<AdminUserResponse> getUsers(Pageable pageable);

    AdminUserResponse updateRole(Long userId, Role role);

    void deleteUser(Long userId);
}
