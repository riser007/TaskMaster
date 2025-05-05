package com.taskmaster.dto.task;

import lombok.Data;

@Data
public class TaskAssignRequest {
    // Can be null to unassign the task
    private Long assigneeId;
}