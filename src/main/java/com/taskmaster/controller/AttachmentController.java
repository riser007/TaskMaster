package com.taskmaster.controller;

import com.taskmaster.dto.attachment.AttachmentResponse;
import com.taskmaster.security.UserPrincipal;
import com.taskmaster.service.AttachmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AttachmentController {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentController.class);

    @Autowired private AttachmentService attachmentService;

    @PostMapping("/tasks/{taskId}/attachments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        AttachmentResponse response = attachmentService.attachFileToTask(taskId, file, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/tasks/{taskId}/attachments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttachmentResponse>> getAttachmentsForTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<AttachmentResponse> attachments = attachmentService.getAttachmentsByTaskId(taskId, currentUser.getId());
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    @PreAuthorize("isAuthenticated()") // Add auth check here now
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal UserPrincipal currentUser, // Get user for auth check
            HttpServletRequest request) {

        Resource resource = attachmentService.downloadAttachmentFile(attachmentId, currentUser.getId());

        // Determine content type (same logic as before)
        String contentType = null;
        String fileName = "downloaded_file"; // Default filename
        try {
            // Attempt to get filename from resource if possible (may not always work)
            if (resource.getFilename() != null) {
                fileName = resource.getFilename(); // Use original filename if available via Resource
            }
            // Get content type from servlet context
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type for attachment ID: {}", attachmentId);
        } catch (Exception e) {
            logger.warn("Could not get filename or file path from resource for attachment ID: {}", attachmentId);
        }

        if (contentType == null) {
            contentType = "application/octet-stream"; // Fallback
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteAttachment(
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        attachmentService.deleteAttachment(attachmentId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}