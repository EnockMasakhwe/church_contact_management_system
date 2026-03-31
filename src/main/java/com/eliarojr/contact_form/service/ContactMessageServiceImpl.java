package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.entity.ContactMessage;
import com.eliarojr.contact_form.repository.ContactMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ContactMessageServiceImpl implements ContactMessageService {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    private final Logger log = LoggerFactory.getLogger(ContactMessageServiceImpl.class);


    @Override
    public ContactMessage saveMessage(ContactMessage contactMessage) {
        contactMessage.setCreatedAt(LocalDateTime.now());
        contactMessage.setStatus("NEW");
        log.info("Saving message from: {}", contactMessage.getEmail());
        return contactMessageRepository.save(contactMessage);
    }
}
