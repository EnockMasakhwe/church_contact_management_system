package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.AppointmentResponse;
import com.eliarojr.contact_form.entity.Appointment;
import com.eliarojr.contact_form.entity.enums.AppointmentStatus;
import com.eliarojr.contact_form.entity.enums.MessageStatus;
import com.eliarojr.contact_form.exception.ResourceNotFoundException;
import com.eliarojr.contact_form.exception.BadRequestException;
import com.eliarojr.contact_form.repository.AppointmentRepository;
import com.eliarojr.contact_form.repository.UserRepository;
import com.eliarojr.contact_form.entity.enums.Role;
import com.eliarojr.contact_form.event.NotificationEmailEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.super-admin-email}")
    private String superAdminEmail;

    //Admin view
    public List<AppointmentResponse> getAllAppointments() {

        return appointmentRepository.findAllByOrderByStartTimeAsc()
                .stream()
                .map(a -> new AppointmentResponse(
                        a.getId(),
                        a.getStartTime(),
                        a.getEndTime(),
                        a.getStatus().name(),

                        //include user info
                        a.getMessage().getUser().getUsername(),
                        a.getMessage().getUser().getEmail(),
                        a.getMessage().getMessage()
                ))
                .toList();
    }

    public Page<AppointmentResponse> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAllByOrderByStartTimeAsc(pageable)
                .map(a -> new AppointmentResponse(
                        a.getId(), a.getStartTime(), a.getEndTime(), a.getStatus().name(),
                        a.getMessage().getUser().getUsername(),
                        a.getMessage().getUser().getEmail(),
                        a.getMessage().getMessage()));
    }

    //Public view
    public List<AppointmentResponse> getPublicAppointments() {

        return appointmentRepository.findAllByOrderByStartTimeAsc()
                .stream()
                .filter(a ->
                        a.getStatus() == AppointmentStatus.PENDING ||
                                a.getStatus() == AppointmentStatus.APPROVED
                )
                .map(a -> new AppointmentResponse(
                        a.getId(),
                        a.getStartTime(),
                        a.getEndTime(),
                        a.getStatus().name(),
                        null,
                        null,
                        null
                ))
                .toList();
    }

    //Update status
    @Transactional
    public AppointmentResponse updateStatus(Long id, AppointmentStatus status) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() == status) {
            return mapToResponse(appointment);
        }

        boolean validTransition = switch (appointment.getStatus()) {
            case PENDING -> status == AppointmentStatus.APPROVED || status == AppointmentStatus.REJECTED;
            case APPROVED -> status == AppointmentStatus.COMPLETED;
            case REJECTED, COMPLETED, CANCELLED -> false;
        };
        if (!validTransition) {
            throw new BadRequestException("Appointment status cannot change from "
                    + appointment.getStatus() + " to " + status);
        }

        appointment.setStatus(status);
        if (status == AppointmentStatus.REJECTED || status == AppointmentStatus.COMPLETED) {
            appointment.setReservedSlot(null);
        } else if (status == AppointmentStatus.APPROVED) {
            appointment.setReservedSlot(appointment.getStartTime());
            try {
                appointmentRepository.saveAndFlush(appointment);
            } catch (DataIntegrityViolationException exception) {
                throw new BadRequestException("Selected time slot is already reserved");
            }
        }

        //Auto-sync message
        if (appointment.getMessage() != null) {
            switch (status) {
                case APPROVED -> appointment.getMessage().setStatus(MessageStatus.IN_PROGRESS);
                case COMPLETED, REJECTED, CANCELLED -> appointment.getMessage().setStatus(MessageStatus.CLOSED);
                case PENDING -> appointment.getMessage().setStatus(MessageStatus.READ);
            }
        }

        AppointmentResponse response = mapToResponse(appointment);

        //Send email AFTER save
        if (status != AppointmentStatus.PENDING && status != AppointmentStatus.CANCELLED
                && appointment.getMessage() != null && appointment.getMessage().getUser() != null) {

            String userEmail = appointment.getMessage().getUser().getEmail();
            String username = appointment.getMessage().getUser().getUsername();

            String subject;
            String body;

            switch (status) {
                case APPROVED -> {
                    subject = "Appointment Approved";
                    body = "<h3>Hello " + escapeHtml(username) + ",</h3>"
                            + "<p>Your appointment has been approved.</p>"
                            + "<p><strong>Requested time:</strong> " + escapeHtml(appointment.getStartTime().toString()) + "</p>"
                            + "<p><strong>Location:</strong> " + escapeHtml(appointment.getMessage().getLocation()) + "</p>";
                }
                case REJECTED -> {
                    subject = "Appointment Rejected";
                    body = "<h3>Hello " + escapeHtml(username) + ",</h3>"
                            + "<p>Your appointment request was rejected. Please contact the church if you need help arranging another visit.</p>"
                            + "<p><strong>Requested time:</strong> " + escapeHtml(appointment.getStartTime().toString()) + "</p>";
                }
                case COMPLETED -> {
                    subject = "Appointment Completed";
                    body = "<h3>Hello " + escapeHtml(username) + ",</h3>"
                            + "<p>Your appointment has been marked as completed.</p>"
                            + "<p><strong>Requested time:</strong> " + escapeHtml(appointment.getStartTime().toString()) + "</p>";
                }
                default -> throw new IllegalStateException("Unexpected appointment status: " + status);
            }

            eventPublisher.publishEvent(new NotificationEmailEvent(userEmail, subject, body));
        }

        return response;
    }

    @Transactional
    public AppointmentResponse cancelByCurrentUser(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Appointment not found");
        }

        Appointment appointment = appointmentRepository.findByIdAndMessage_User_EmailIgnoreCase(
                        id, authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Past appointments cannot be cancelled");
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.APPROVED) {
            throw new BadRequestException("Only pending or approved appointments can be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setReservedSlot(null);
        if (appointment.getMessage() != null) {
            appointment.getMessage().setStatus(MessageStatus.CLOSED);
        }
        publishCancellationToAdmins(appointment);
        return mapToResponse(appointment);
    }

    private void publishCancellationToAdmins(Appointment appointment) {
        String subject = "Appointment Cancelled";
        String body = """
                <h3>Appointment cancelled by member</h3>
                <p><strong>Member:</strong> %s</p>
                <p><strong>Email:</strong> %s</p>
                <p><strong>Phone:</strong> %s</p>
                <p><strong>Requested time:</strong> %s</p>
                <p><strong>Location:</strong> %s</p>
                """.formatted(
                escapeHtml(appointment.getMessage().getUser().getUsername()),
                escapeHtml(appointment.getMessage().getUser().getEmail()),
                escapeHtml(appointment.getMessage().getPhone()),
                escapeHtml(appointment.getStartTime().toString()),
                escapeHtml(appointment.getMessage().getLocation())
        );
        userRepository.findAdminRecipients(
                        List.of(Role.ROLE_ADMIN, Role.ROLE_SUPER_ADMIN), superAdminEmail)
                .forEach(admin -> eventPublisher.publishEvent(
                        new NotificationEmailEvent(admin.getEmail(), subject, body)));
    }

    private String escapeHtml(String value) {
        return HtmlUtils.htmlEscape(value == null ? "-" : value);
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus().name(),
                appointment.getMessage().getUser().getUsername(),
                appointment.getMessage().getUser().getEmail(),
                appointment.getMessage().getMessage()
        );
    }
}