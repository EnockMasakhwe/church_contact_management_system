package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.AppointmentResponse;
import com.eliarojr.contact_form.dto.MessageResponse;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<MessageResponse> getMyMessages(Pageable pageable);
    Page<AppointmentResponse> getMyAppointments(Pageable pageable);
    AppointmentResponse cancelMyAppointment(Long appointmentId);
}
