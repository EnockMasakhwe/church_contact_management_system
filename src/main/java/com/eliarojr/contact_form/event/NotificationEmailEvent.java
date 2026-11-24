package com.eliarojr.contact_form.event;

public record NotificationEmailEvent(String email, String subject, String htmlBody) {
}
