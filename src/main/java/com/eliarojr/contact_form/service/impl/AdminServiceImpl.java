package com.eliarojr.contact_form.service.impl;

import com.eliarojr.contact_form.entity.ContactMessage;
import com.eliarojr.contact_form.entity.enums.MessageStatus;
import com.eliarojr.contact_form.exception.ResourceNotFoundException;
import com.eliarojr.contact_form.repository.AppointmentRepository;
import com.eliarojr.contact_form.repository.ContactMessageRepository;
import com.eliarojr.contact_form.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ContactMessageRepository contactMessageRepository;

    private final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Override
    public List<ContactMessage> getAllMessages() {
        log.info("Fetching all messages from DB");
        return contactMessageRepository.findAll();
    }

    @Override
    public ContactMessage getMessageById(Long id) {
        log.info("Fetching message from DB by Id");
        return contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found!"));
    }

    @Override
    public ContactMessage updateStatus(Long id, MessageStatus status) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found!"));

        log.info("Message found; updating status");
        message.setStatus(status);

        return contactMessageRepository.save(message);
    }

    @Override
    public void deleteMessage(Long id) {
        if (!contactMessageRepository.existsById(id)){
            throw new ResourceNotFoundException("Message not found!");
        }
        log.info("Message found; deleting");
        contactMessageRepository.deleteById(id);
    }
}
