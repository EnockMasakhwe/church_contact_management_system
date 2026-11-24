package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.entity.Appointment;
import com.eliarojr.contact_form.entity.enums.AppointmentStatus;
import com.eliarojr.contact_form.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AppointmentReservationInitializer implements ApplicationRunner {

    private final AppointmentRepository appointmentRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<Appointment> activeAppointments = appointmentRepository
                .findByStatusInAndReservedSlotIsNull(
                        List.of(AppointmentStatus.PENDING, AppointmentStatus.APPROVED));
        for (Appointment appointment : activeAppointments) {
            appointment.setReservedSlot(appointment.getStartTime());
            appointmentRepository.saveAndFlush(appointment);
        }
    }
}
