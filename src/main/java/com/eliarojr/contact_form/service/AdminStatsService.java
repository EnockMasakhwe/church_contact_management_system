package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.AdminStatsResponse;
import com.eliarojr.contact_form.entity.enums.AppointmentStatus;
import com.eliarojr.contact_form.entity.enums.MessageStatus;
import com.eliarojr.contact_form.repository.AppointmentRepository;
import com.eliarojr.contact_form.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final MessageRepository messageRepository;
    private final AppointmentRepository appointmentRepository;

    public AdminStatsResponse getStats() {

        return new AdminStatsResponse(
                messageRepository.count(),
                messageRepository.countByStatus(MessageStatus.NEW),
                messageRepository.countByStatus(MessageStatus.IN_PROGRESS),
                messageRepository.countByStatus(MessageStatus.CLOSED),

                appointmentRepository.count(),
                appointmentRepository.countByStatus(AppointmentStatus.PENDING),
                appointmentRepository.countByStatus(AppointmentStatus.APPROVED),
                appointmentRepository.countByStatus(AppointmentStatus.COMPLETED)
        );
    }
}