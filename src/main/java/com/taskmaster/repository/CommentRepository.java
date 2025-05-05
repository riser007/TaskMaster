package com.taskmaster.repository;

import com.taskmaster.model.Comment;
import com.taskmaster.model.Task;
import com.taskmaster.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByTaskId(Long taskId, Pageable pageable);

    // Optional: Find comment by ID and Author for deletion authorization checks
    Optional<Comment> findByIdAndAuthor(Long id, User author);
}