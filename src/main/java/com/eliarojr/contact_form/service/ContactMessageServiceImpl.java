package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.entity.ContactMessage;
import com.eliarojr.contact_form.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContactMessageServiceImpl implements ContactMessageService {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Override
    public ContactMessage saveMessage(ContactMessage contactMessage) {
        return contactMessageRepository.save(contactMessage);
    }
}
