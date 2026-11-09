package com.eliarojr.contact_form.dto;

public record AdminStatsResponse(
        long totalMessages,
        long newMessages,
        long inProgressMessages,
        long closedMessages,
        long totalAppointments,
        long pendingAppointments,
        long approvedAppointments,
        long completedAppointments
) {}
