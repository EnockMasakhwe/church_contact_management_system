package com.eliarojr.contact_form.service.impl;

import com.eliarojr.contact_form.dto.ContactMessageRequest;
import com.eliarojr.contact_form.entity.Appointment;
import com.eliarojr.contact_form.entity.enums.AppointmentStatus;
import com.eliarojr.contact_form.entity.ContactMessage;
import com.eliarojr.contact_form.entity.enums.MessageStatus;
import com.eliarojr.contact_form.exception.ResourceNotFoundException;
import com.eliarojr.contact_form.repository.AppointmentRepository;
import com.eliarojr.contact_form.repository.ContactMessageRepository;
import com.eliarojr.contact_form.service.ContactMessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.eliarojr.contact_form.entity.enums.MessageType.VISITATION_REQUEST;
import static org.hibernate.internal.util.StringHelper.isBlank;

@Service
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final AppointmentRepository appointmentRepository;

    private final Logger log = LoggerFactory.getLogger(ContactMessageServiceImpl.class);

    private ContactMessage mapToEntity(ContactMessageRequest request) {

        ContactMessage message = new ContactMessage();

        message.setName(request.getName());
        message.setEmail(request.getEmail());
        message.setPhone(request.getPhone());
        message.setMessage(request.getMessage());
        message.setType(request.getType());
        message.setStatus(MessageStatus.NEW);
        message.setCreatedAt(LocalDateTime.now());

        message.setLocation(request.getLocation());
        message.setPreferredDateTime(request.getPreferredDateTime());

        return message;
    }


    @Transactional
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

                LocalDateTime start = request.getPreferredDateTime();
                LocalDateTime end = start.plusHours(1);

                List<Appointment> conflicts =
                        appointmentRepository.findConflictingAppointments(start, end);

                if (!conflicts.isEmpty()) {
                    throw new RuntimeException("Time slot already booked");
                }
            }

            case PRAYER_REQUEST -> {
                // optional
            }

            case BEREAVEMENT, GENERAL -> {
                // no extra validation
            }

            default -> throw new IllegalArgumentException("Invalid message type");
        }

        ContactMessage message = mapToEntity(request);
        ContactMessage savedMessage = contactMessageRepository.save(message);

        if (request.getType() == VISITATION_REQUEST) {

            log.info("Creating appointment for message ID: {}", savedMessage.getId());

            LocalDateTime start = request.getPreferredDateTime();

            Appointment appointment = new Appointment();
            appointment.setStartTime(start);
            appointment.setEndTime(start.plusHours(1));
            appointment.setStatus(AppointmentStatus.PENDING);
            appointment.setMessage(savedMessage);

            appointmentRepository.save(appointment);
        }

        return savedMessage;
    }

}
