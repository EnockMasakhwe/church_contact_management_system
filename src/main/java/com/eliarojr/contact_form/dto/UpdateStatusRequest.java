package com.eliarojr.contact_form.dto;

import com.eliarojr.contact_form.entity.enums.MessageStatus;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    private MessageStatus status;
}
