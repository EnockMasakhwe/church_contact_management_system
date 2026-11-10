package com.eliarojr.contact_form.event;

import com.eliarojr.contact_form.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AuthEmailEventListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAuthEmail(AuthEmailEvent event) {
        switch (event.type()) {
            case VERIFICATION ->
                    emailService.sendVerificationEmail(event.email(), event.token());
            case PASSWORD_RESET ->
                    emailService.sendPasswordResetEmail(event.email(), event.token());
        }
    }
}
