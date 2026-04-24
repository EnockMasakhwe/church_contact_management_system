package com.eliarojr.contact_form.service.impl;

import com.eliarojr.contact_form.dto.AppointmentResponse;
import com.eliarojr.contact_form.dto.MessageResponse;
import com.eliarojr.contact_form.repository.AppointmentRepository;
import com.eliarojr.contact_form.repository.MessageRepository;
import com.eliarojr.contact_form.repository.UserRepository;
import com.eliarojr.contact_form.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final MessageRepository messageRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        String email = ((UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();

        return userRepository.findByEmail(email)
                //.orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    @Override
    public List<MessageResponse> getMyMessages() {

        Long userId = getCurrentUserId();

        return messageRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(msg -> new MessageResponse(
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
                ))
                .toList();
    }

    @Override
    public List<AppointmentResponse> getMyAppointments() {

        Long userId = getCurrentUserId();

        return appointmentRepository.findByMessage_User_IdOrderByStartTimeAsc(userId)
                .stream()
                .map(a -> new AppointmentResponse(
                        a.getId(),
                        a.getStartTime(),
                        a.getEndTime(),
                        a.getStatus().name(),
                        null, // don't expose name again
                        null,
                        a.getMessage().getMessage()
                ))
                .toList();
    }
}