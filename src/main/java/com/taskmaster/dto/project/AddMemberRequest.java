package com.taskmaster.dto.project;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMemberRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

}