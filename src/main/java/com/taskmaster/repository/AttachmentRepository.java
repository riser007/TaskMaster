package com.taskmaster.repository;

import com.taskmaster.model.Attachment;
import com.taskmaster.model.Task;
import com.taskmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByTaskId(Long taskId);

    // Optional: Find attachment by ID and Uploader for deletion authorization checks
    Optional<Attachment> findByIdAndUploader(Long id, User uploader);

    // Find by file path to potentially avoid duplicates or manage storage
    Optional<Attachment> findByFilePath(String filePath);
}