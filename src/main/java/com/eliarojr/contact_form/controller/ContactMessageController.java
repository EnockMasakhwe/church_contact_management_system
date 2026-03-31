package com.eliarojr.contact_form.controller;

import com.eliarojr.contact_form.entity.ContactMessage;
import com.eliarojr.contact_form.service.ContactMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactMessageController {

    @Autowired
    private ContactMessageService contactMessageService;

    @PostMapping("/message")
    public ContactMessage saveMessage(@RequestBody ContactMessage contactMessage){
        return contactMessageService.saveMessage(contactMessage);
    }
}
