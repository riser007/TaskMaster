package com.taskmaster.controller;

import com.taskmaster.dto.PagedResponse;
import com.taskmaster.dto.comment.CommentCreateRequest;
import com.taskmaster.dto.comment.CommentResponse;
import com.taskmaster.security.UserPrincipal;
import com.taskmaster.service.CommentService;
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
@RequestMapping("/api") // Comments accessed via tasks or directly by ID
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/tasks/{taskId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentCreateRequest commentRequest,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        CommentResponse comment = commentService.addComment(taskId, commentRequest, currentUser.getId());
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @GetMapping("/tasks/{taskId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<CommentResponse>> getCommentsForTask(
            @PathVariable Long taskId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size, // More comments per page?
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir, // Show oldest first typically
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        // TODO: Validate sortBy field
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PagedResponse<CommentResponse> comments = commentService.getCommentsByTaskId(taskId, currentUser.getId(), pageable);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        commentService.deleteComment(commentId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}