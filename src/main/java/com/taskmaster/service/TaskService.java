package com.taskmaster.service;

import com.taskmaster.dto.PagedResponse;
import com.taskmaster.dto.task.TaskCreateRequest;
import com.taskmaster.dto.task.TaskResponse;
import com.taskmaster.dto.task.TaskUpdateRequest; // Create this DTO
import com.taskmaster.model.common.TaskStatus;
import org.springframework.data.domain.Pageable;


public interface TaskService {

    PagedResponse<TaskResponse> getTasksByProjectId(Long projectId, Long currentUserId, String statusFilter, String searchTerm, Pageable pageable);

    PagedResponse<TaskResponse> getTasksAssignedToUser(Long userId, Pageable pageable);

    TaskResponse getTaskById(Long projectId, Long taskId, Long currentUserId);

    TaskResponse createTask(Long projectId, TaskCreateRequest taskRequest, Long creatorUserId);

    TaskResponse updateTask(Long projectId, Long taskId, TaskUpdateRequest taskRequest, Long currentUserId);

    // Method to update only the status (e.g., mark as completed)
    TaskResponse updateTaskStatus(Long projectId, Long taskId, TaskStatus newStatus, Long currentUserId);

    // Method to assign/reassign a task
    TaskResponse assignTask(Long projectId, Long taskId, Long assigneeId, Long currentUserId);

    void deleteTask(Long projectId, Long taskId, Long currentUserId);

    // --- Helper or Internal Methods (Could be private in Impl) ---
    void verifyUserMembership(Long projectId, Long userId); // Throws exception if not member

}