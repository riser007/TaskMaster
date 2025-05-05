package com.taskmaster.dto.user;

import lombok.Data;
import java.time.Instant;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Instant createdAt;
    // Add other non-sensitive fields as needed
}