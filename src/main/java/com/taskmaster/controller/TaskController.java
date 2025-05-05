package com.taskmaster.controller;

import com.taskmaster.dto.PagedResponse;
import com.taskmaster.dto.task.TaskCreateRequest;
import com.taskmaster.dto.task.TaskResponse;
import com.taskmaster.dto.task.TaskStatusUpdateRequest; // Create this DTO { TaskStatus status; }
import com.taskmaster.dto.task.TaskUpdateRequest; // Create this DTO
import com.taskmaster.dto.task.TaskAssignRequest; // Create this DTO { Long assigneeId; }
import com.taskmaster.model.common.TaskStatus;
import com.taskmaster.security.UserPrincipal; // Get logged-in user
import com.taskmaster.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api") // Base path
public class TaskController {

    @Autowired
    private TaskService taskService;

    // --- Get Tasks ---

    @GetMapping("/projects/{projectId}/tasks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<TaskResponse>> getTasksByProject(
            @PathVariable Long projectId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String searchTerm,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        // TODO: Validate sortBy field against allowed Task fields
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PagedResponse<TaskResponse> tasks = taskService.getTasksByProjectId(
                projectId, currentUser.getId(), status, searchTerm, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/users/me/tasks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<TaskResponse>> getMyAssignedTasks(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "dueDate") String sortBy, // Default sort for my tasks
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        // TODO: Validate sortBy field
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PagedResponse<TaskResponse> tasks = taskService.getTasksAssignedToUser(currentUser.getId(), pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/projects/{projectId}/tasks/{taskId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TaskResponse task = taskService.getTaskById(projectId, taskId, currentUser.getId());
        return ResponseEntity.ok(task);
    }

    // --- Create Task ---

    @PostMapping("/projects/{projectId}/tasks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskCreateRequest taskRequest,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TaskResponse createdTask = taskService.createTask(projectId, taskRequest, currentUser.getId());
        // Return 201 Created status
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    // --- Update Task ---

    @PutMapping("/projects/{projectId}/tasks/{taskId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest taskRequest, // Create this DTO
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TaskResponse updatedTask = taskService.updateTask(projectId, taskId, taskRequest, currentUser.getId());
        return ResponseEntity.ok(updatedTask);
    }

    // --- Update Task Status (Partial Update) ---

    @PatchMapping("/projects/{projectId}/tasks/{taskId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskStatusUpdateRequest statusRequest, // DTO containing only the new status
            @AuthenticationPrincipal UserPrincipal currentUser) {

        TaskResponse updatedTask = taskService.updateTaskStatus(projectId, taskId, statusRequest.getStatus(), currentUser.getId());
        return ResponseEntity.ok(updatedTask);
    }

    // --- Assign Task (Partial Update) ---

    @PatchMapping("/projects/{projectId}/tasks/{taskId}/assignee")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskAssignRequest assignRequest, // DTO containing assigneeId (can be null to unassign)
            @AuthenticationPrincipal UserPrincipal currentUser) {

        TaskResponse updatedTask = taskService.assignTask(projectId, taskId, assignRequest.getAssigneeId(), currentUser.getId());
        return ResponseEntity.ok(updatedTask);
    }


    // --- Delete Task ---

    @DeleteMapping("/projects/{projectId}/tasks/{taskId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        taskService.deleteTask(projectId, taskId, currentUser.getId());
        // Return 204 No Content on successful deletion
        return ResponseEntity.noContent().build();
    }
}