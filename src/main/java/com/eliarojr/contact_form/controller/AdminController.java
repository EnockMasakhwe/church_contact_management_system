package com.eliarojr.contact_form.controller;

import com.eliarojr.contact_form.dto.UpdateStatusRequest;
import com.eliarojr.contact_form.entity.ContactMessage;
import com.eliarojr.contact_form.service.ContactMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ContactMessageService contactMessageService;

    @GetMapping("messages")
    public ResponseEntity<List<ContactMessage>> getAllMessages(){
        return ResponseEntity.ok(contactMessageService.getAllMessages());
    }


    @GetMapping("/messages/{id}")
    public ResponseEntity<ContactMessage> getMessageById(@PathVariable Long id){
        ContactMessage message = contactMessageService.getMessageById(id);
        return  ResponseEntity.ok(message);
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long id) {
        contactMessageService.deleteMessage(id);
        return ResponseEntity.ok("Message deleted successfully");
    }

    @PutMapping("/messages/{id}/status")
    public ResponseEntity<ContactMessage> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(contactMessageService.updateStatus(id, request.getStatus()));
    }

}
