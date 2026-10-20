package com.eliarojr.contact_form.dto;

import com.eliarojr.contact_form.entity.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull Role role) {
}
