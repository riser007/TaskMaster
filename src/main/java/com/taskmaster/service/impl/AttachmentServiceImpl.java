package com.taskmaster.service.impl;

import com.taskmaster.dto.attachment.AttachmentResponse;
import com.taskmaster.dto.user.UserSummaryResponse;
import com.taskmaster.exception.ResourceNotFoundException;
import com.taskmaster.model.Attachment;
import com.taskmaster.model.Task;
import com.taskmaster.model.User;
import com.taskmaster.repository.AttachmentRepository;
import com.taskmaster.repository.TaskRepository;
import com.taskmaster.repository.UserRepository;
import com.taskmaster.service.AttachmentService;
import com.taskmaster.service.FileStorageService;
import com.taskmaster.service.TaskService; // For project membership check
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    @Autowired private AttachmentRepository attachmentRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private TaskRepository taskRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TaskService taskService; // For project membership check

    @Override
    @Transactional
    public AttachmentResponse attachFileToTask(Long taskId, MultipartFile file, Long uploaderUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        User uploader = userRepository.findById(uploaderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", uploaderUserId));

        // Authorization: Check if uploader is member of the task's project
        taskService.verifyUserMembership(task.getProject().getId(), uploaderUserId);

        // Store file
        String subDir = "project_" + task.getProject().getId() + "/task_" + taskId; // Example subdirectory structure
        String filePath = fileStorageService.storeFile(file, subDir);

        // Create entity
        Attachment attachment = Attachment.builder()
                .fileName(StringUtils.cleanPath(file.getOriginalFilename()))
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(filePath)
                .task(task)
                .uploader(uploader)
                .build();

        Attachment savedAttachment = attachmentRepository.save(attachment);
        logger.info("User {} uploaded attachment {} for task {}", uploaderUserId, savedAttachment.getId(), taskId);
        return mapToAttachmentResponse(savedAttachment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByTaskId(Long taskId, Long currentUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        // Authorization: Check if user is member of the task's project
        taskService.verifyUserMembership(task.getProject().getId(), currentUserId);

        List<Attachment> attachments = attachmentRepository.findByTaskId(taskId);
        return attachments.stream()
                .map(this::mapToAttachmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Resource downloadAttachmentFile(Long attachmentId, Long currentUserId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", attachmentId));

        // Authorization: Check if user is member of the task's project
        taskService.verifyUserMembership(attachment.getTask().getProject().getId(), currentUserId);

        return fileStorageService.loadFileAsResource(attachment.getFilePath());
    }


    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId, Long currentUserId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", attachmentId));

        // Authorization: Check if user is project member AND (uploader OR project owner)
        taskService.verifyUserMembership(attachment.getTask().getProject().getId(), currentUserId);
        boolean isOwner = attachment.getTask().getProject().getOwner().getId().equals(currentUserId);
        boolean isUploader = attachment.getUploader().getId().equals(currentUserId);

        if (!isOwner && !isUploader) {
            logger.warn("Access denied for user {} attempting to delete attachment {}", currentUserId, attachmentId);
            throw new AccessDeniedException("Only the uploader or project owner can delete attachments.");
        }

        // 1. Delete file from storage *before* deleting DB record
        try {
            fileStorageService.deleteFile(attachment.getFilePath());
        } catch (Exception e) {
            // Log the error but proceed to delete DB record maybe? Or rethrow?
            logger.error("Failed to delete attachment file {} from storage for attachment ID {}. DB record will still be deleted.",
                    attachment.getFilePath(), attachmentId, e);
            // Depending on policy, you might rethrow or just log and continue.
            // throw new RuntimeException("Failed to delete attachment file from storage.", e);
        }


        // 2. Delete entity from database
        attachmentRepository.delete(attachment);
        logger.info("User {} deleted attachment {}", currentUserId, attachmentId);
    }

    // --- Mapping ---
    private AttachmentResponse mapToAttachmentResponse(Attachment attachment) {
        AttachmentResponse dto = new AttachmentResponse();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileType(attachment.getFileType());
        dto.setFileSize(attachment.getFileSize());
        dto.setTaskId(attachment.getTask().getId());
        dto.setCreatedAt(attachment.getCreatedAt());
        if (attachment.getUploader() != null) {
            dto.setUploader(new UserSummaryResponse(
                    attachment.getUploader().getId(),
                    attachment.getUploader().getUsername(),
                    attachment.getUploader().getFirstName(),
                    attachment.getUploader().getLastName()
            ));
        }
        // Optional: Construct download URL
        // dto.setDownloadUrl("/api/attachments/" + attachment.getId() + "/download");
        return dto;
    }
}