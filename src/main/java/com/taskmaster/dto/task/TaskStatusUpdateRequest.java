package com.taskmaster.dto.task;

import com.taskmaster.model.common.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskStatusUpdateRequest {
    @NotNull(message = "Status cannot be null")
    private TaskStatus status;
}