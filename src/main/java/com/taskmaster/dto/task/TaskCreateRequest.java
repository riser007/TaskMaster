package com.taskmaster.dto.task;

import com.taskmaster.model.common.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskCreateRequest {
    @NotBlank(message = "Task title cannot be blank")
    @Size(max = 255, message = "Task title cannot exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description is too long") // Example size limit
    private String description;

    // Status might be set by default, or optionally provided
    private TaskStatus status; // If provided, validate it's a valid status

    @FutureOrPresent(message = "Due date must be today or in the future")
    private LocalDate dueDate;

    // Assignee ID is optional during creation, can be assigned later
    private Long assigneeId;

    // Project ID is usually derived from the path parameter (e.g., /api/projects/{projectId}/tasks)
    // and not included in the request body itself.
}