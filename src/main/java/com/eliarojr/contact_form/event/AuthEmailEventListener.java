package com.eliarojr.contact_form.event;

import com.eliarojr.contact_form.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEmailEventListener {

    private final EmailService emailService;

    @Async
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAuthEmail(AuthEmailEvent event) {
        switch (event.type()) {
            case VERIFICATION ->
                    emailService.sendVerificationEmail(event.email(), event.token());
            case PASSWORD_RESET ->
                    emailService.sendPasswordResetEmail(event.email(), event.token());
        }
    }

    @Recover
    public void logFailedAuthEmail(RuntimeException exception, AuthEmailEvent event) {
        log.error("Failed to deliver {} email to {} after retries",
                event.type(), event.email(), exception);
    }
}
