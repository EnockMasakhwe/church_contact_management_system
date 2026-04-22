package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.entity.Message;
import com.eliarojr.contact_form.entity.enums.MessageStatus;

import java.util.List;

public interface AdminService {
    List<Message> getAllMessages();

    Message getMessageById(Long id);

    Message updateStatus(Long id, MessageStatus status);

    void deleteMessage(Long id);
}
