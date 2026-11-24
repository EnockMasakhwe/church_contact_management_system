package com.eliarojr.contact_form.service.impl;

import com.eliarojr.contact_form.dto.MessageRequest;
import com.eliarojr.contact_form.dto.MessageResponse;
import com.eliarojr.contact_form.entity.Appointment;
import com.eliarojr.contact_form.entity.enums.AppointmentStatus;
import com.eliarojr.contact_form.entity.Message;
import com.eliarojr.contact_form.entity.enums.MessageStatus;
import com.eliarojr.contact_form.entity.enums.MessageType;
import com.eliarojr.contact_form.entity.enums.Role;
import com.eliarojr.contact_form.entity.User;
import com.eliarojr.contact_form.event.NotificationEmailEvent;
import com.eliarojr.contact_form.exception.BadRequestException;
import com.eliarojr.contact_form.exception.UnauthorizedException;
import com.eliarojr.contact_form.repository.AppointmentRepository;
import com.eliarojr.contact_form.repository.MessageRepository;
import com.eliarojr.contact_form.repository.UserRepository;
import com.eliarojr.contact_form.service.AppointmentSchedule;
import com.eliarojr.contact_form.service.MessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.List;
import static org.hibernate.internal.util.StringHelper.isBlank;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.super-admin-email}")
    private String superAdminEmail;

    @Transactional
    @Override
    public MessageResponse sendMessage(MessageRequest request) {

        //GET LOGGED-IN USER
        String email = ((UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        //VALIDATION
        if (request.getType() == MessageType.VISITATION_REQUEST) {

            if (isBlank(request.getLocation())) {
                throw new BadRequestException("Location required");
            }

            if (request.getPreferredDateTime() == null) {
                throw new BadRequestException("Date/time required");
            }

            LocalDateTime start = request.getPreferredDateTime();
            AppointmentSchedule.validate(start, LocalDateTime.now());
            LocalDateTime end = start.plusHours(1);

            List<Appointment> conflicts =
                    appointmentRepository.findConflictingAppointments(
                            start,
                            end,
                            List.of(AppointmentStatus.PENDING, AppointmentStatus.APPROVED)
                    );
            if (!conflicts.isEmpty()) {
                throw new BadRequestException("Selected time slot already booked");
            }
        }

        //SAVE MESSAGE
        Message message = new Message();

        message.setUser(user); //KEY
        message.setMessage(request.getMessage());
        message.setType(request.getType());
        message.setStatus(MessageStatus.NEW);
        message.setLocation(request.getLocation());
        message.setPreferredDateTime(request.getPreferredDateTime());
        message.setPhone(request.getPhone());

        Message saved = messageRepository.save(message);


        //CREATE APPOINTMENT
        if (request.getType() == MessageType.VISITATION_REQUEST) {

            LocalDateTime start = request.getPreferredDateTime();

            Appointment appointment = new Appointment();
            appointment.setStartTime(start);
            appointment.setEndTime(start.plusHours(1));
            appointment.setReservedSlot(start);
            appointment.setStatus(AppointmentStatus.PENDING);
            appointment.setMessage(saved);

            try {
                appointmentRepository.saveAndFlush(appointment);
            } catch (DataIntegrityViolationException exception) {
                throw new BadRequestException("Selected time slot already booked");
            }
        }

        // Notify all admins, including the configured superadmin.
        String subject = "New Message Received";

        String body = """
             <h3>New Message Submitted</h3>
             <p><strong>User:</strong> %s</p>
             <p><strong>Email:</strong> %s</p>
             <p><strong>Phone:</strong> %s</p>
             <p><strong>Type:</strong> %s</p>
             <p><strong>Location:</strong> %s</p>
             <p><strong>Requested time:</strong> %s</p>
             <p><strong>Message:</strong><br>%s</p>
        """.formatted(
                escapeHtml(user.getUsername()),
                escapeHtml(user.getEmail()),
                escapeHtml(request.getPhone()),
                request.getType().name(),
                escapeHtml(request.getLocation() == null ? "-" : request.getLocation()),
                escapeHtml(request.getPreferredDateTime() == null ? "-" : request.getPreferredDateTime().toString()),
                escapeHtml(request.getMessage()).replace("\n", "<br>")
        );

        List<Role> adminRoles = List.of(Role.ROLE_ADMIN, Role.ROLE_SUPER_ADMIN);
        userRepository.findAdminRecipients(adminRoles, superAdminEmail)
                .forEach(admin -> eventPublisher.publishEvent(
                        new NotificationEmailEvent(admin.getEmail(), subject, body)));

        //Response
        return mapToResponse(saved);
    }

    private String escapeHtml(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
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
