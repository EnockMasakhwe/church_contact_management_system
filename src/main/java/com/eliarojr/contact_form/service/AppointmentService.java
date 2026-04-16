package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.AppointmentResponse;
import com.eliarojr.contact_form.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public List<AppointmentResponse> getAllAppointments() {

        return appointmentRepository.findAllByOrderByStartTimeAsc()
                .stream()
                .map(a -> new AppointmentResponse(
                        a.getStartTime(),
                        a.getEndTime(),
                        a.getStatus().name()
                ))
                .toList();
    }
}
