package com.eliarojr.contact_form.controller;

import com.eliarojr.contact_form.dto.AdminStatsResponse;
import com.eliarojr.contact_form.dto.AdminUserResponse;
import com.eliarojr.contact_form.dto.MessageResponse;
import com.eliarojr.contact_form.dto.UpdateUserRoleRequest;
import com.eliarojr.contact_form.dto.UpdateStatusRequest;
import com.eliarojr.contact_form.entity.Message;
import com.eliarojr.contact_form.service.AdminService;
import com.eliarojr.contact_form.service.AdminStatsService;
import com.eliarojr.contact_form.service.AdminUserService;
import com.eliarojr.contact_form.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<AdminUserResponse>> getUsers() {
        return ResponseEntity.ok(adminUserService.getUsers());
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminUserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRoleRequest request) {
        return ResponseEntity.ok(adminUserService.updateRole(id, request.role()));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/messages")
    public ResponseEntity<List<MessageResponse>> getAllMessages(){
        return ResponseEntity.ok(adminService.getAllMessages());
    }


    /* @GetMapping("/messages/{id}")
    public ResponseEntity<ContactMessage> getMessageById(@PathVariable Long id){
        ContactMessage message = contactMessageService.getMessageById(id);
        return  ResponseEntity.ok(message);
    } */

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long id) {
        adminService.deleteMessage(id);
        return ResponseEntity.ok("Message deleted successfully");
    }

    @PutMapping("/messages/{id}/status")
    public ResponseEntity<MessageResponse> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(adminService.updateStatus(id, request.getStatus()));
    }

    @GetMapping("/stats")
    public AdminStatsResponse getStats() {
        return adminStatsService.getStats();
    }

}
