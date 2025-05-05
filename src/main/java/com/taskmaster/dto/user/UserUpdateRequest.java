package com.taskmaster.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    // Allow updating subset of fields
    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Size(max = 100)
    @Email
    private String email; // Updating email might require a verification step in a real app

    // Optional: Password update (requires more logic like current password check)
    // @Size(min = 6, max = 100)
    // private String newPassword;
    // private String currentPassword;
}