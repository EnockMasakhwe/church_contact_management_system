package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.MessageResponse;
import com.eliarojr.contact_form.entity.Message;
import com.eliarojr.contact_form.entity.enums.MessageStatus;

import java.util.List;

public interface AdminService {
    List<MessageResponse> getAllMessages();

    MessageResponse getMessageById(Long id);

    MessageResponse updateStatus(Long id, MessageStatus status);

    void deleteMessage(Long id);
}
