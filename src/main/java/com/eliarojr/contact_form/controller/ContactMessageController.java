package com.eliarojr.contact_form.controller;

import com.eliarojr.contact_form.entity.ContactMessage;
import com.eliarojr.contact_form.service.ContactMessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactMessageController {

    @Autowired
    private ContactMessageService contactMessageService;

    @PostMapping("/message")
    public ResponseEntity <ContactMessage> saveMessage(@RequestBody @Valid ContactMessage contactMessage){
        ContactMessage saved = contactMessageService.saveMessage(contactMessage);
        return ResponseEntity.ok(saved);
    }
}
