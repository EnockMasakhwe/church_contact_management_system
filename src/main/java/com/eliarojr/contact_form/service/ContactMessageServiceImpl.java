package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.entity.ContactMessage;
import com.eliarojr.contact_form.exception.ResourceNotFoundException;
import com.eliarojr.contact_form.repository.ContactMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContactMessageServiceImpl implements ContactMessageService{
    @Autowired
    private ContactMessageRepository contactMessageRepository;

    private final Logger log = LoggerFactory.getLogger(ContactMessageServiceImpl.class);


    @Override
    public ContactMessage saveMessage(ContactMessage message) {
        message.setCreatedAt(LocalDateTime.now());
        message.setStatus("NEW");
        log.info("Saving message from: {}", message.getEmail());
        return contactMessageRepository.save(message);
    }

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
    public ContactMessage updateMessage(Long id, ContactMessage updatedMessage) {
        ContactMessage existing = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found!"));

        log.info("Message found; updating status");
        if (updatedMessage.getStatus() != null){
            existing.setStatus(updatedMessage.getStatus());
        }
        /*existing.setName(updatedMessage.getName());
        existing.setEmail(updatedMessage.getEmail());
        existing.setPhoneNumber(updatedMessage.getPhoneNumber());
        existing.setMessage(updatedMessage.getMessage());
        existing.setStatus(updatedMessage.getStatus());*/

        return contactMessageRepository.save(existing);
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
