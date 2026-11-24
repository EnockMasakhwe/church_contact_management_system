package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.MessageResponse;
import com.eliarojr.contact_form.entity.Message;
import com.eliarojr.contact_form.entity.enums.MessageStatus;
import com.eliarojr.contact_form.entity.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    Page<MessageResponse> getAllMessages(MessageStatus status, MessageType type, String search, Pageable pageable);

    MessageResponse getMessageById(Long id);

    MessageResponse updateStatus(Long id, MessageStatus status);

    void deleteMessage(Long id);
}
