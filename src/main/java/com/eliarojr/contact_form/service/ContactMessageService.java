package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.ContactMessageRequest;
import com.eliarojr.contact_form.entity.ContactMessage;
import com.eliarojr.contact_form.entity.enums.MessageStatus;

import java.util.List;

public interface ContactMessageService {
    ContactMessage sendMessage(ContactMessageRequest request);

}
