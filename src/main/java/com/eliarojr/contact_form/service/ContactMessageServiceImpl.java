package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.ContactMessageRequest;
import com.eliarojr.contact_form.entity.ContactMessage;
import com.eliarojr.contact_form.entity.MessageStatus;
import com.eliarojr.contact_form.exception.ResourceNotFoundException;
import com.eliarojr.contact_form.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.hibernate.internal.util.StringHelper.isBlank;

@Service
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService{

    private final ContactMessageRepository contactMessageRepository;

    private final Logger log = LoggerFactory.getLogger(ContactMessageServiceImpl.class);


    @Override
    public ContactMessage sendMessage(ContactMessageRequest request) {
        log.info("Saving message from: {}", request.getEmail());

        switch (request.getType()) {

            case VISITATION_REQUEST -> {
                if (isBlank(request.getLocation())) {
                    throw new IllegalArgumentException("Location required");
                }
                if (request.getPreferredDateTime() == null) {
                    throw new IllegalArgumentException("Date/time required");
                }
            }

            case PRAYER_REQUEST -> {
                // Optional: allow date but not required
            }

            case BEREAVEMENT, GENERAL -> {
                // No extra fields needed
            }
        }

        ContactMessage message = new ContactMessage();

        message.setName(request.getName());
        message.setEmail(request.getEmail());
        message.setPhone(request.getPhone());
        message.setType(request.getType());
        message.setMessage(request.getMessage());
        message.setLocation(request.getLocation());
        message.setPreferredDateTime(request.getPreferredDateTime());
        message.setStatus(MessageStatus.NEW);
        message.setCreatedAt(LocalDateTime.now());

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
