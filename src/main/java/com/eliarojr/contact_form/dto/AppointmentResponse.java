package com.eliarojr.contact_form.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}
