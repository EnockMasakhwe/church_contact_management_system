package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.entity.ContactMessage;
import com.eliarojr.contact_form.entity.MessageStatus;

import java.util.List;

public interface ContactMessageService {
    ContactMessage saveMessage(ContactMessage message);

    List<ContactMessage> getAllMessages();

    ContactMessage getMessageById(Long id);

    ContactMessage updateStatus(Long id, MessageStatus status);

    void deleteMessage(Long id);
}
