package com.eliarojr.contact_form.service.impl;

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
    public List<Message> getAllMessages() {
        log.info("Fetching all messages from DB");
        return messageRepository.findAll();
    }

    @Override
    public Message getMessageById(Long id) {
        log.info("Fetching message from DB by Id");
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found!"));
    }

    @Override
    public Message updateStatus(Long id, MessageStatus status) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found!"));

        log.info("Message found; updating status");
        message.setStatus(status);

        return messageRepository.save(message);
    }

    @Override
    public void deleteMessage(Long id) {
        if (!messageRepository.existsById(id)){
            throw new ResourceNotFoundException("Message not found!");
        }
        log.info("Message found; deleting");
        messageRepository.deleteById(id);
    }
}
