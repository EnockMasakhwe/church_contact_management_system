package com.eliarojr.contact_form;

import com.eliarojr.contact_form.entity.Appointment;
import com.eliarojr.contact_form.entity.Message;
import com.eliarojr.contact_form.entity.User;
import com.eliarojr.contact_form.entity.enums.AppointmentStatus;
import com.eliarojr.contact_form.entity.enums.MessageStatus;
import com.eliarojr.contact_form.exception.BadRequestException;
import com.eliarojr.contact_form.repository.AppointmentRepository;
import com.eliarojr.contact_form.service.AppointmentService;
import com.eliarojr.contact_form.service.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private EmailService emailService;
    @InjectMocks private AppointmentService appointmentService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsInvalidAdminStatusTransition() {
        Appointment appointment = appointment(AppointmentStatus.PENDING);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(BadRequestException.class,
                () -> appointmentService.updateStatus(1L, AppointmentStatus.COMPLETED));

        assertEquals(AppointmentStatus.PENDING, appointment.getStatus());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void approvalRetainsTheReservationAndAdvancesMessageStatus() {
        Appointment appointment = appointment(AppointmentStatus.PENDING);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.saveAndFlush(appointment)).thenReturn(appointment);

        appointmentService.updateStatus(1L, AppointmentStatus.APPROVED);

        assertEquals(AppointmentStatus.APPROVED, appointment.getStatus());
        assertEquals(appointment.getStartTime(), appointment.getReservedSlot());
        assertEquals(MessageStatus.IN_PROGRESS, appointment.getMessage().getStatus());
        verify(emailService).sendEmail(eq("member@example.com"), eq("Appointment Approved"), any());
    }

    @Test
    void cancellationIsScopedToCurrentMemberAndReleasesFutureSlot() {
        Appointment appointment = appointment(AppointmentStatus.PENDING);
        appointment.setStartTime(LocalDateTime.now().plusDays(1));
        appointment.setEndTime(appointment.getStartTime().plusHours(1));
        appointment.setReservedSlot(appointment.getStartTime());
        when(appointmentRepository.findByIdAndMessage_User_EmailIgnoreCase(1L, "member@example.com"))
                .thenReturn(Optional.of(appointment));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("member@example.com", "n/a", Collections.emptyList()));

        appointmentService.cancelByCurrentUser(1L);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        assertEquals(MessageStatus.CLOSED, appointment.getMessage().getStatus());
        assertNull(appointment.getReservedSlot());
        verify(appointmentRepository).findByIdAndMessage_User_EmailIgnoreCase(1L, "member@example.com");
    }

    @Test
    void cancellationRejectsPastAppointment() {
        Appointment appointment = appointment(AppointmentStatus.APPROVED);
        appointment.setStartTime(LocalDateTime.now().minusHours(1));
        when(appointmentRepository.findByIdAndMessage_User_EmailIgnoreCase(1L, "member@example.com"))
                .thenReturn(Optional.of(appointment));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("member@example.com", "n/a", Collections.emptyList()));

        assertThrows(BadRequestException.class, () -> appointmentService.cancelByCurrentUser(1L));

        assertEquals(AppointmentStatus.APPROVED, appointment.getStatus());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    private Appointment appointment(AppointmentStatus status) {
        User user = new User();
        user.setUsername("Member");
        user.setEmail("member@example.com");

        Message message = new Message();
        message.setMessage("Visit request");
        message.setLocation("Church");
        message.setStatus(MessageStatus.NEW);
        message.setUser(user);

        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStartTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0));
        appointment.setEndTime(appointment.getStartTime().plusHours(1));
        appointment.setReservedSlot(appointment.getStartTime());
        appointment.setStatus(status);
        appointment.setMessage(message);
        return appointment;
    }
}
