package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.entity.ContactMessage;

public interface ContactMessageService {
    ContactMessage saveMessage(ContactMessage contactMessage);
}
