package com.eliarojr.contact_form.controller;

import com.eliarojr.contact_form.dto.ContactMessageRequest;
import com.eliarojr.contact_form.entity.ContactMessage;
import com.eliarojr.contact_form.service.ContactMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    @PostMapping("/messages")
    public ResponseEntity<ContactMessage> sendMessage(@Valid @RequestBody ContactMessageRequest request){
        ContactMessage saved = contactMessageService.sendMessage(request);
        return ResponseEntity.ok(saved);
    }

}
