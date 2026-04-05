package com.eliarojr.contact_form.controller;

import com.eliarojr.contact_form.entity.ContactMessage;
import com.eliarojr.contact_form.service.ContactMessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
public class ContactMessageController {
    @Autowired
    private ContactMessageService contactMessageService;

    @PostMapping("/messages")
    public ResponseEntity<ContactMessage> saveMessage(@Valid @RequestBody ContactMessage message){
        System.out.println("Validation passed");
        ContactMessage saved = contactMessageService.saveMessage(message);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/messages")
    public ResponseEntity<List<ContactMessage>> getAllMessages(){
        List<ContactMessage> messages = contactMessageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<ContactMessage> getMessageById(@PathVariable Long id){
        ContactMessage message = contactMessageService.getMessageById(id);
        return  ResponseEntity.ok(message);
    }

    @PutMapping("/messages/{id}")
    public ResponseEntity<ContactMessage> updateMessage(@PathVariable Long id, @RequestBody ContactMessage updatedMessage){
        ContactMessage message = contactMessageService.updateMessage(id, updatedMessage);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long id){
        contactMessageService.deleteMessage(id);
        return ResponseEntity.ok("Message deleted successfully!");
    }

}
