package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.AppointmentResponse;
import com.eliarojr.contact_form.entity.Appointment;
import com.eliarojr.contact_form.entity.enums.AppointmentStatus;
import com.eliarojr.contact_form.entity.enums.MessageStatus;
import com.eliarojr.contact_form.exception.ResourceNotFoundException;
import com.eliarojr.contact_form.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public Appointment updateStatus(Long id, AppointmentStatus status) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        appointment.setStatus(status);

        //Auto-sync message
        if (appointment.getMessage() != null) {
            switch (status) {
                case APPROVED -> appointment.getMessage().setStatus(MessageStatus.IN_PROGRESS);
                case COMPLETED -> appointment.getMessage().setStatus(MessageStatus.CLOSED);
                case REJECTED -> appointment.getMessage().setStatus(MessageStatus.CLOSED);
            }
        }

        Appointment saved = appointmentRepository.save(appointment);

        //Send email AFTER save
        if (saved.getMessage() != null && saved.getMessage().getUser() != null) {

            String userEmail = saved.getMessage().getUser().getEmail();
            String username = saved.getMessage().getUser().getUsername();

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
                default -> {
                    subject = "Appointment Update";
                    body = "<p>Status updated</p>";
                }
            }

            emailService.sendEmail(userEmail, subject, body);
        }

        return saved;
    }
}