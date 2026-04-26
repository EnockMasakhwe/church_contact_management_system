package com.eliarojr.contact_form.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminStatsResponse {

    private long totalMessages;
    private long newMessages;
    private long inProgressMessages;
    private long closedMessages;

    private long totalAppointments;
    private long pendingAppointments;
    private long approvedAppointments;
    private long completedAppointments;
}