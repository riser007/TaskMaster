package com.taskmaster.dto.comment;

import com.taskmaster.dto.user.UserSummaryResponse;
import lombok.Data;
import java.time.Instant;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private Long taskId;
    private UserSummaryResponse author;
    private Instant createdAt;
    private Instant updatedAt;
}