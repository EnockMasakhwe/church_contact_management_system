package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.AdminUserResponse;
import com.eliarojr.contact_form.entity.enums.Role;

import java.util.List;

public interface AdminUserService {
    List<AdminUserResponse> getUsers();

    AdminUserResponse updateRole(Long userId, Role role);

    void deleteUser(Long userId);
}
