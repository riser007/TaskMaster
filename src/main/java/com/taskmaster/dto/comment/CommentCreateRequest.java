package com.taskmaster.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateRequest {
    @NotBlank(message = "Comment content cannot be blank")
    @Size(max = 2000, message = "Comment cannot exceed 2000 characters") // Example limit
    private String content;
}