package com.taskmaster.service.impl;

import com.taskmaster.dto.PagedResponse;
import com.taskmaster.dto.task.TaskCreateRequest;
import com.taskmaster.dto.task.TaskResponse;
import com.taskmaster.dto.task.TaskUpdateRequest;
import com.taskmaster.exception.BadRequestException;
import com.taskmaster.exception.ResourceNotFoundException;
import com.taskmaster.model.Project;
import com.taskmaster.model.Task;
import com.taskmaster.model.User;
import com.taskmaster.model.common.TaskStatus;
import com.taskmaster.repository.ProjectRepository;
import com.taskmaster.repository.TaskRepository;
import com.taskmaster.repository.UserRepository;
import com.taskmaster.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // For filtering/searching
import org.springframework.security.access.AccessDeniedException; // Or custom authorization exception
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// TODO: Implement Specification builders for dynamic filtering/searching

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired private TaskRepository taskRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private UserRepository userRepository;
    // TODO: Inject ModelMapper or write manual mapping methods

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getTasksByProjectId(Long projectId, Long currentUserId, String statusFilter, String searchTerm, Pageable pageable) {
        verifyUserMembership(projectId, currentUserId); // Check access first

        // TODO: Build a dynamic Specification based on statusFilter and searchTerm
        Specification<Task> spec = Specification.where(TaskSpecifications.belongsToProject(projectId));
        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                TaskStatus status = TaskStatus.valueOf(statusFilter.toUpperCase());
                spec = spec.and(TaskSpecifications.hasStatus(status));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status filter value: " + statusFilter);
            }
        }
        if (searchTerm != null && !searchTerm.isBlank()) {
            spec = spec.and(TaskSpecifications.containsText(searchTerm));
        }

        Page<Task> tasksPage = taskRepository.findAll(spec, pageable);

        List<TaskResponse> taskResponses = tasksPage.getContent().stream()
                .map(this::mapToTaskResponse) // Use mapping function
                .collect(Collectors.toList());

        return new PagedResponse<>(
                taskResponses,
                tasksPage.getNumber(),
                tasksPage.getSize(),
                tasksPage.getTotalElements(),
                tasksPage.getTotalPages(),
                tasksPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getTasksAssignedToUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId)); // Should not happen for logged-in user

        Page<Task> tasksPage = taskRepository.findByAssignee(user, pageable);

        List<TaskResponse> taskResponses = tasksPage.getContent().stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                taskResponses,
                tasksPage.getNumber(),
                tasksPage.getSize(),
                tasksPage.getTotalElements(),
                tasksPage.getTotalPages(),
                tasksPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long projectId, Long taskId, Long currentUserId) {
        verifyUserMembership(projectId, currentUserId);
        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));
        return mapToTaskResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse createTask(Long projectId, TaskCreateRequest taskRequest, Long creatorUserId) {
        verifyUserMembership(projectId, creatorUserId); // Ensure creator is part of the project

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorUserId)); // Should exist

        User assignee = null;
        if (taskRequest.getAssigneeId() != null) {
            assignee = userRepository.findById(taskRequest.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", taskRequest.getAssigneeId()));
            // Optional: Check if assignee is also a member of the project
            if (!projectRepository.existsByIdAndMembersContaining(projectId, assignee)) {
                throw new BadRequestException("Assignee is not a member of this project.");
            }
        }

        Task task = Task.builder()
                .title(taskRequest.getTitle())
                .description(taskRequest.getDescription())
                .dueDate(taskRequest.getDueDate())
                .status(taskRequest.getStatus() != null ? taskRequest.getStatus() : TaskStatus.OPEN) // Default to OPEN
                .project(project)
                .assignee(assignee)
                // Note: Auditable fields (createdAt, updatedAt) are set automatically
                .build();

        Task savedTask = taskRepository.save(task);
        logger.info("User {} created task {} in project {}", creatorUserId, savedTask.getId(), projectId);
        return mapToTaskResponse(savedTask);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long projectId, Long taskId, TaskUpdateRequest taskRequest, Long currentUserId) {
        verifyUserMembership(projectId, currentUserId); // Ensure updater is part of the project

        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));

        // TODO: Add more granular authorization? Only assignee or project owner can update?
        // Example: if (!task.getAssignee().getId().equals(currentUserId) && !task.getProject().getOwner().getId().equals(currentUserId)) { throw ... }

        // Update fields from request DTO (use ModelMapper or manual mapping)
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setStatus(taskRequest.getStatus());
        task.setDueDate(taskRequest.getDueDate());

        // Handle assignee update separately if needed (e.g., via assignTask method)
        if (taskRequest.getAssigneeId() != null) {
            if (!taskRequest.getAssigneeId().equals(task.getAssignee() == null ? null : task.getAssignee().getId())) {
                assignTaskInternal(task, taskRequest.getAssigneeId()); // Internal helper
            }
        } else if (task.getAssignee() != null) {
            // If assigneeId is null in request, unassign the task
            task.setAssignee(null);
        }

        Task updatedTask = taskRepository.save(task);
        logger.info("User {} updated task {}", currentUserId, updatedTask.getId());
        return mapToTaskResponse(updatedTask);
    }


    @Override
    @Transactional
    public TaskResponse updateTaskStatus(Long projectId, Long taskId, TaskStatus newStatus, Long currentUserId) {
        verifyUserMembership(projectId, currentUserId);
        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));

        // TODO: Add authorization: Who can change status? Assignee? Any member?
        // Example check: if (!task.getAssignee().getId().equals(currentUserId)) throw new AccessDeniedException(...);

        if (task.getStatus() == newStatus) {
            return mapToTaskResponse(task); // No change needed
        }

        // Optional: Add logic for valid status transitions (e.g., cannot go from COMPLETED back to OPEN easily)

        task.setStatus(newStatus);
        Task updatedTask = taskRepository.save(task);
        logger.info("User {} updated status of task {} to {}", currentUserId, taskId, newStatus);
        return mapToTaskResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse assignTask(Long projectId, Long taskId, Long assigneeId, Long currentUserId) {
        verifyUserMembership(projectId, currentUserId); // Check assigner is member

        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));

        // TODO: Authorization: Who can assign? Any member? Project Owner?
        // Example: if (!task.getProject().getOwner().getId().equals(currentUserId)) throw AccessDeniedException(...);

        assignTaskInternal(task, assigneeId); // Use internal helper

        Task updatedTask = taskRepository.save(task);
        logger.info("User {} assigned task {} to user {}", currentUserId, taskId, assigneeId);
        return mapToTaskResponse(updatedTask);
    }

    // Internal helper to avoid code duplication
    private void assignTaskInternal(Task task, Long assigneeId) {
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("User (Assignee)", "id", assigneeId));
            // Ensure assignee is member of the project
            if (!projectRepository.existsByIdAndMembersContaining(task.getProject().getId(), assignee)) {
                throw new BadRequestException("Assignee is not a member of this project.");
            }
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null); // Unassign if assigneeId is null
        }
    }


    @Override
    @Transactional
    public void deleteTask(Long projectId, Long taskId, Long currentUserId) {
        verifyUserMembership(projectId, currentUserId); // Check user is member

        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));

        // TODO: Authorization: Who can delete? Project Owner? Creator? Assignee?
        if (!task.getProject().getOwner().getId().equals(currentUserId)) { // Example: Only owner can delete
            throw new AccessDeniedException("Only the project owner can delete tasks.");
        }

        // TODO: Handle related entities if needed (comments, attachments are cascaded by default)
        // If using cloud storage, you might need to delete files from storage here *before* deleting the attachment entity.

        taskRepository.delete(task);
        logger.info("User {} deleted task {}", currentUserId, taskId);
    }


    // --- Helper Methods ---

    @Override
    public void verifyUserMembership(Long projectId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (!projectRepository.existsByIdAndMembersContaining(projectId, user)) {
            logger.warn("Access denied: User {} is not a member of project {}", userId, projectId);
            throw new AccessDeniedException("User is not a member of the project " + projectId);
            // Or throw ResourceNotFound if you want to hide project existence:
            // throw new ResourceNotFoundException("Project", "id", projectId);
        }
    }

    // TODO: Implement mapping logic (Manual or using ModelMapper)
    private TaskResponse mapToTaskResponse(Task task) {
        TaskResponse res = new TaskResponse();
        res.setId(task.getId());
        res.setTitle(task.getTitle());
        res.setDescription(task.getDescription());
        res.setStatus(task.getStatus());
        res.setDueDate(task.getDueDate());
        res.setProjectId(task.getProject().getId());
        res.setCreatedAt(task.getCreatedAt());
        res.setUpdatedAt(task.getUpdatedAt());

        if (task.getAssignee() != null) {
            // Create UserSummaryResponse DTO to avoid exposing full User details
            // UserSummaryResponse assigneeSummary = new UserSummaryResponse(task.getAssignee().getId(), task.getAssignee().getUsername());
            // res.setAssignee(assigneeSummary);
            res.setAssignee(null); // Placeholder: Implement UserSummaryResponse and mapping
        }

        // Example counts (requires loading collections or dedicated count queries)
        // res.setCommentCount(task.getComments() != null ? task.getComments().size() : 0);
        // res.setAttachmentCount(task.getAttachments() != null ? task.getAttachments().size() : 0);
        res.setCommentCount(0); // Placeholder
        res.setAttachmentCount(0); // Placeholder


        return res;
    }

}


class TaskSpecifications {
    public static Specification<Task> belongsToProject(Long projectId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("project").get("id"), projectId);
    }
    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }
    public static Specification<Task> containsText(String searchTerm) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + searchTerm.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + searchTerm.toLowerCase() + "%")
                );
    }

}