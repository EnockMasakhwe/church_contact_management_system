package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.entity.ContactMessage;

import java.util.List;

public interface ContactMessageService {
    ContactMessage saveMessage(ContactMessage message);

    List<ContactMessage> getAllMessages();

    ContactMessage getMessageById(Long id);

    ContactMessage updateMessage(Long id, ContactMessage updatedMessage);

    void deleteMessage(Long id);
}
