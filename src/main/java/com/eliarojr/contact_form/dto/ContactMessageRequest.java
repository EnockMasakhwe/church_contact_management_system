package com.eliarojr.contact_form.dto;

import com.eliarojr.contact_form.entity.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContactMessageRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(\\+254|0)[0-9]{9}$",
            message = "Invalid Kenyan phone number"
    )
    private String phone;

    @NotNull(message = "Message type is required")
    private MessageType type;

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 1000, message = "Message too long")
    private String message;

    @Size(max = 255, message = "Location too long")
    private String location;

    @Future(message = "Preferred time must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime preferredDateTime;

}
