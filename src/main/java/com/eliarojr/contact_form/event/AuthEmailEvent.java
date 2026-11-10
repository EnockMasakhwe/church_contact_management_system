package com.eliarojr.contact_form.event;

public record AuthEmailEvent(String email, String token, Type type) {

    public enum Type {
        VERIFICATION,
        PASSWORD_RESET
    }
}
