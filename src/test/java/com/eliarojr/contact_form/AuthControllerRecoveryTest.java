package com.eliarojr.contact_form;

import com.eliarojr.contact_form.controller.AuthController;
import com.eliarojr.contact_form.dto.ForgotPasswordRequest;
import com.eliarojr.contact_form.service.AuthService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuthControllerRecoveryTest {

    @Test
    void passwordResetConfirmationDoesNotRevealWhetherEmailExists() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);

        ForgotPasswordRequest knownAccount = request("member@example.com");
        ForgotPasswordRequest unknownAccount = request("unknown@example.com");

        String knownResponse = controller.forgotPassword(knownAccount).getBody();
        String unknownResponse = controller.forgotPassword(unknownAccount).getBody();

        assertEquals(knownResponse, unknownResponse);
        assertEquals("If an account exists for that email, a password reset link will be sent.", knownResponse);
        verify(authService).createPasswordResetToken("member@example.com");
        verify(authService).createPasswordResetToken("unknown@example.com");
    }

    private ForgotPasswordRequest request(String email) {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(email);
        return request;
    }
}
