package com.taskmaster.dto.task;

import com.taskmaster.dto.user.UserSummaryResponse; // Create a smaller User DTO
import com.taskmaster.model.common.TaskStatus;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data // Using Lombok for boilerplate
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;
    private Long projectId;
    // Avoid sending full User object, use a summary DTO
    private UserSummaryResponse assignee;
    private Instant createdAt;
    private Instant updatedAt;
    // Optionally include counts or summaries of comments/attachments
    private int commentCount;
    private int attachmentCount;
}