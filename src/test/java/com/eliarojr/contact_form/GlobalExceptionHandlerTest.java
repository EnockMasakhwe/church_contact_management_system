package com.eliarojr.contact_form;

import com.eliarojr.contact_form.dto.RegisterRequest;
import com.eliarojr.contact_form.exception.BadRequestException;
import com.eliarojr.contact_form.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void validationErrorsAreReturnedTogether() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","email":"invalid","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Username is required")))
                .andExpect(jsonPath("$.message", containsString("Invalid email format")))
                .andExpect(jsonPath("$.message", containsString("Password is required")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void malformedJsonReturnsConsistentClientError() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is invalid or malformed."))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void unexpectedErrorsDoNotExposeInternalMessages() throws Exception {
        mockMvc.perform(post("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Something went wrong. Try again."))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.not(containsString("internal detail"))))
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void expectedApplicationErrorsKeepTheirStatusAndMessage() throws Exception {
        mockMvc.perform(post("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void methodSecurityErrorsReturnForbidden() throws Exception {
        mockMvc.perform(post("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied."))
                .andExpect(jsonPath("$.status").value(403));
    }

    @RestController
    public static class TestController {

        @PostMapping("/test/validation")
        public void validate(@RequestBody @Valid RegisterRequest request) {
        }

        @PostMapping("/test/unexpected")
        public void failUnexpectedly() {
            throw new IllegalStateException("internal detail");
        }

        @PostMapping("/test/bad-request")
        public void failWithBadRequest() {
            throw new BadRequestException("Invalid request");
        }

        @PostMapping("/test/forbidden")
        public void failWithAccessDenied() {
            throw new AccessDeniedException("internal authorization detail");
        }
    }
}
