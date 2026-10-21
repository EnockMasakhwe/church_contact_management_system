package com.eliarojr.contact_form.dto;

import com.eliarojr.contact_form.entity.enums.MessageStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    @NotNull(message = "Status is required")
    private MessageStatus status;
}
