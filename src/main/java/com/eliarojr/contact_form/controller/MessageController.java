package com.eliarojr.contact_form.controller;

import com.eliarojr.contact_form.dto.MessageRequest;
import com.eliarojr.contact_form.dto.MessageResponse;
import com.eliarojr.contact_form.entity.Message;
import com.eliarojr.contact_form.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody MessageRequest request){
        MessageResponse saved = messageService.sendMessage(request);
        return ResponseEntity.ok(saved);
    }

}
