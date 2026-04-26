package com.eliarojr.contact_form.service.impl;

import com.eliarojr.contact_form.dto.MessageRequest;
import com.eliarojr.contact_form.dto.MessageResponse;
import com.eliarojr.contact_form.entity.Appointment;
import com.eliarojr.contact_form.entity.User;
import com.eliarojr.contact_form.entity.enums.AppointmentStatus;
import com.eliarojr.contact_form.entity.Message;
import com.eliarojr.contact_form.entity.enums.MessageStatus;
import com.eliarojr.contact_form.entity.enums.MessageType;
import com.eliarojr.contact_form.repository.AppointmentRepository;
import com.eliarojr.contact_form.repository.MessageRepository;
import com.eliarojr.contact_form.repository.UserRepository;
import com.eliarojr.contact_form.service.EmailService;
import com.eliarojr.contact_form.service.MessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.eliarojr.contact_form.entity.enums.MessageType.VISITATION_REQUEST;
import static org.hibernate.internal.util.StringHelper.isBlank;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Transactional
    @Override
    public MessageResponse sendMessage(MessageRequest request) {

        //GET LOGGED-IN USER
        String email = ((UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();

        User user = userRepository.findByEmail(email);
                //.orElseThrow(() -> new RuntimeException("User not found"));

        MessageType type = MessageType.valueOf(String.valueOf(request.getType()));

        //VALIDATION
        if (type == MessageType.VISITATION_REQUEST) {

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

        //SAVE MESSAGE
        Message message = new Message();

        message.setUser(user); //KEY
        message.setMessage(request.getMessage());
        message.setType(type);
        message.setStatus(MessageStatus.NEW);
        message.setCreatedAt(LocalDateTime.now());

        message.setLocation(request.getLocation());
        message.setPreferredDateTime(request.getPreferredDateTime());
        message.setPhone(request.getPhone());

        Message saved = messageRepository.save(message);

        // Notify admin
        String adminEmail = "enockmasakhwe@gmail.com";

        String subject = "New Message Received";

        String body = """
             <h3>New Message Submitted</h3>
             <p><strong>User:</strong> %s</p>
             <p><strong>Email:</strong> %s</p>
             <p><strong>Type:</strong> %s</p>
             <p><strong>Message:</strong> %s</p>
        """.formatted(
                user.getUsername(),
                user.getEmail(),
                type.name(),
                request.getMessage()
        );

        emailService.sendEmail(adminEmail, subject, body);

        //CREATE APPOINTMENT
        if (type == MessageType.VISITATION_REQUEST) {

            LocalDateTime start = request.getPreferredDateTime();

            Appointment appointment = new Appointment();
            appointment.setStartTime(start);
            appointment.setEndTime(start.plusHours(1));
            appointment.setStatus(AppointmentStatus.PENDING);
            appointment.setMessage(saved);

            appointmentRepository.save(appointment);
        }

        //Response
        return mapToResponse(saved);
    }

    //MAPPER
    private MessageResponse mapToResponse(Message msg) {
        return new MessageResponse(
                msg.getId(),
                msg.getMessage(),
                msg.getType().name(),
                msg.getStatus().name(),
                msg.getLocation(),
                msg.getPreferredDateTime(),
                msg.getPhone(),
                msg.getUser().getUsername(),
                msg.getUser().getEmail(),
                msg.getCreatedAt()
        );
    }


}
