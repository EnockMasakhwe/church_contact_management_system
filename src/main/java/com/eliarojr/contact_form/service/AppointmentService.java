package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.AppointmentResponse;
import com.eliarojr.contact_form.entity.Appointment;
import com.eliarojr.contact_form.entity.enums.AppointmentStatus;
import com.eliarojr.contact_form.entity.enums.MessageStatus;
import com.eliarojr.contact_form.exception.ResourceNotFoundException;
import com.eliarojr.contact_form.exception.BadRequestException;
import com.eliarojr.contact_form.repository.AppointmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;

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
                    body = "<h3>Hello " + username + ",</h3><p>Your appointment has been approved.</p>";
                }
                case REJECTED -> {
                    subject = "Appointment Rejected";
                    body = "<h3>Hello " + username + ",</h3><p>Your appointment was rejected.</p>";
                }
                case COMPLETED -> {
                    subject = "Appointment Completed";
                    body = "<h3>Hello " + username + ",</h3><p>Your appointment is completed.</p>";
                }
                default -> throw new IllegalStateException("Unexpected appointment status: " + status);
            }

            emailService.sendEmail(userEmail, subject, body);
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
        return mapToResponse(appointment);
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