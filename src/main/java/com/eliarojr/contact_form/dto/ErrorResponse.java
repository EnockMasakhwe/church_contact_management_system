package com.eliarojr.contact_form.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        int status,
        LocalDateTime timestamp
) {}
