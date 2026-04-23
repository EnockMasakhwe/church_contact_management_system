package com.eliarojr.contact_form.service.impl;

import com.eliarojr.contact_form.dto.MessageResponse;
import com.eliarojr.contact_form.entity.Message;
import com.eliarojr.contact_form.entity.enums.MessageStatus;
import com.eliarojr.contact_form.exception.ResourceNotFoundException;
import com.eliarojr.contact_form.repository.MessageRepository;
import com.eliarojr.contact_form.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final MessageRepository messageRepository;
    private final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Override
    public List<MessageResponse> getAllMessages() {
        log.info("Fetching all messages from DB");

        return messageRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public MessageResponse getMessageById(Long id) {
        log.info("Fetching message by ID: {}", id);

        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found!"));

        return mapToResponse(message);
    }

    @Override
    public MessageResponse updateStatus(Long id, MessageStatus status) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found!"));

        log.info("Updating message status. ID: {}, Status: {}", id, status);

        message.setStatus(status);

        Message updated = messageRepository.save(message);

        return mapToResponse(updated);
    }

    @Override
    public void deleteMessage(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Message not found!");
        }

        log.info("Deleting message ID: {}", id);
        messageRepository.deleteById(id);
    }

    //MAPPER
    private MessageResponse mapToResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getMessage(),
                message.getType().name(),
                message.getStatus().name(),
                message.getLocation(),
                message.getPreferredDateTime(),
                message.getPhone(),
                message.getUser() != null ? message.getUser().getUsername() : "-",
                message.getUser() != null ? message.getUser().getEmail() : "-",
                message.getCreatedAt()
        );
    }
}