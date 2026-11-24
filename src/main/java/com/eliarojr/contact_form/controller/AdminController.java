package com.eliarojr.contact_form.controller;

import com.eliarojr.contact_form.dto.AdminStatsResponse;
import com.eliarojr.contact_form.dto.AdminUserResponse;
import com.eliarojr.contact_form.dto.MessageResponse;
import com.eliarojr.contact_form.dto.UpdateUserRoleRequest;
import com.eliarojr.contact_form.dto.UpdateStatusRequest;
import com.eliarojr.contact_form.service.AdminService;
import com.eliarojr.contact_form.service.AdminStatsService;
import com.eliarojr.contact_form.service.AdminUserService;
import com.eliarojr.contact_form.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.eliarojr.contact_form.entity.enums.MessageStatus;
import com.eliarojr.contact_form.entity.enums.MessageType;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MessageService messageService;
    private final AdminService adminService;
    private final AdminStatsService adminStatsService;
    private final AdminUserService adminUserService;

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminUserService.getUsers(pageable));
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminUserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRoleRequest request) {
        return ResponseEntity.ok(adminUserService.updateRole(id, request.role()));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/messages")
    public ResponseEntity<Page<MessageResponse>> getAllMessages(
            @RequestParam(required = false) MessageStatus status,
            @RequestParam(required = false) MessageType type,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminService.getAllMessages(status, type, search, pageable));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity.HeadersBuilder<?> deleteMessage(@PathVariable Long id) {
        adminService.deleteMessage(id);
        return ResponseEntity.noContent();
    }

    @PutMapping("/messages/{id}/status")
    public ResponseEntity<MessageResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateStatusRequest request
    ) {
        return ResponseEntity.ok(adminService.updateStatus(id, request.getStatus()));
    }

    @GetMapping("/stats")
    public AdminStatsResponse getStats() {
        return adminStatsService.getStats();
    }

}
