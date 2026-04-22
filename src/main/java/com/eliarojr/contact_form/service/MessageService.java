package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.MessageRequest;
import com.eliarojr.contact_form.dto.MessageResponse;
import com.eliarojr.contact_form.entity.Message;

public interface MessageService {
    MessageResponse sendMessage(MessageRequest request);

}
