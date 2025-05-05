package com.taskmaster.service;

import com.taskmaster.dto.attachment.AttachmentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {

    AttachmentResponse attachFileToTask(Long taskId, MultipartFile file, Long uploaderUserId);

    List<AttachmentResponse> getAttachmentsByTaskId(Long taskId, Long currentUserId);

    Resource downloadAttachmentFile(Long attachmentId, Long currentUserId); // Add user ID for auth check

    void deleteAttachment(Long attachmentId, Long currentUserId);
}