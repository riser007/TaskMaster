package com.taskmaster.service.impl;

import com.taskmaster.dto.PagedResponse;
import com.taskmaster.dto.comment.CommentCreateRequest;
import com.taskmaster.dto.comment.CommentResponse;
import com.taskmaster.dto.user.UserSummaryResponse;
import com.taskmaster.exception.ResourceNotFoundException;
import com.taskmaster.model.Comment;
import com.taskmaster.model.Task;
import com.taskmaster.model.User;
import com.taskmaster.repository.CommentRepository;
import com.taskmaster.repository.TaskRepository;
import com.taskmaster.repository.UserRepository;
import com.taskmaster.service.CommentService;
import com.taskmaster.service.TaskService; // For checking project membership easily
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired private CommentRepository commentRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TaskService taskService; // Re-use membership check

    @Override
    @Transactional
    public CommentResponse addComment(Long taskId, CommentCreateRequest commentRequest, Long authorUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        User author = userRepository.findById(authorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorUserId));

        // Authorization: Check if user is member of the task's project
        taskService.verifyUserMembership(task.getProject().getId(), authorUserId);

        Comment comment = Comment.builder()
                .content(commentRequest.getContent())
                .task(task)
                .author(author)
                .build();

        Comment savedComment = commentRepository.save(comment);
        logger.info("User {} added comment {} to task {}", authorUserId, savedComment.getId(), taskId);
        return mapToCommentResponse(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> getCommentsByTaskId(Long taskId, Long currentUserId, Pageable pageable) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        // Authorization: Check if user is member of the task's project
        taskService.verifyUserMembership(task.getProject().getId(), currentUserId);

        Page<Comment> commentsPage = commentRepository.findByTaskId(taskId, pageable);

        List<CommentResponse> commentResponses = commentsPage.getContent().stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                commentResponses,
                commentsPage.getNumber(),
                commentsPage.getSize(),
                commentsPage.getTotalElements(),
                commentsPage.getTotalPages(),
                commentsPage.isLast()
        );
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        // Authorization: Check if user is the author OR the project owner
        boolean isAuthor = comment.getAuthor().getId().equals(currentUserId);
        boolean isProjectOwner = comment.getTask().getProject().getOwner().getId().equals(currentUserId);

        if (!isAuthor && !isProjectOwner) {
            logger.warn("Access denied for user {} attempting to delete comment {}", currentUserId, commentId);
            throw new AccessDeniedException("You do not have permission to delete this comment.");
        }

        commentRepository.delete(comment);
        logger.info("User {} deleted comment {}", currentUserId, commentId);
    }

    // --- Mapping ---
    private CommentResponse mapToCommentResponse(Comment comment) {
        CommentResponse dto = new CommentResponse();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setTaskId(comment.getTask().getId());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        if (comment.getAuthor() != null) {
            dto.setAuthor(new UserSummaryResponse(
                    comment.getAuthor().getId(),
                    comment.getAuthor().getUsername(),
                    comment.getAuthor().getFirstName(),
                    comment.getAuthor().getLastName()
            ));
        }
        return dto;
    }
}