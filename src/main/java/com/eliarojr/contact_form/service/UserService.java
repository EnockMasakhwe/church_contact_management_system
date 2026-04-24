package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.AppointmentResponse;
import com.eliarojr.contact_form.dto.MessageResponse;

import java.util.List;

public interface UserService {
    List<MessageResponse> getMyMessages();
    List<AppointmentResponse> getMyAppointments();
}
