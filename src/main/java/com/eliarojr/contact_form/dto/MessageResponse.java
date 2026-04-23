package com.eliarojr.contact_form.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {

    private Long id;

    private String message;
    private String type;
    private String status;

    private String location;
    private LocalDateTime preferredDateTime;

    private String phone;

    //from logged-in user
    private String name;
    private String email;

    private LocalDateTime createdAt;
}
