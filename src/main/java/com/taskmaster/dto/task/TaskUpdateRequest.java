package com.taskmaster.dto.task;

import com.taskmaster.model.common.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskUpdateRequest {

    @NotNull(message = "Title cannot be null") // Title must be present, but can be empty if allowed? Adjust if needed.
    @Size(max = 255, message = "Task title cannot exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description is too long")
    private String description;

    @NotNull(message = "Status cannot be null")
    private TaskStatus status; // Require status on update

    @FutureOrPresent(message = "Due date must be today or in the future")
    private LocalDate dueDate;

    // ID of the user to assign the task to. Null means unassigned.
    private Long assigneeId;
}