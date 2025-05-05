package com.taskmaster.service;

import com.taskmaster.dto.PagedResponse;
import com.taskmaster.dto.comment.CommentCreateRequest;
import com.taskmaster.dto.comment.CommentResponse;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    CommentResponse addComment(Long taskId, CommentCreateRequest commentRequest, Long authorUserId);

    PagedResponse<CommentResponse> getCommentsByTaskId(Long taskId, Long currentUserId, Pageable pageable);

    void deleteComment(Long commentId, Long currentUserId);
}