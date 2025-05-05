package com.taskmaster.dto.attachment;

import com.taskmaster.dto.user.UserSummaryResponse;
import lombok.Data;
import java.time.Instant;

@Data
public class AttachmentResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize; // in bytes
    private Long taskId;
    private UserSummaryResponse uploader;
    private Instant createdAt;

}