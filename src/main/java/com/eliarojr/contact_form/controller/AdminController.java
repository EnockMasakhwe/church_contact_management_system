package com.eliarojr.contact_form.controller;

import com.eliarojr.contact_form.dto.MessageResponse;
import com.eliarojr.contact_form.dto.UpdateStatusRequest;
import com.eliarojr.contact_form.entity.Message;
import com.eliarojr.contact_form.service.AdminService;
import com.eliarojr.contact_form.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MessageService messageService;
    private final AdminService adminService;

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

}
