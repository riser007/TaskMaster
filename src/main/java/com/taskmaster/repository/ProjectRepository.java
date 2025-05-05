package com.taskmaster.repository;

import com.taskmaster.model.Project;
import com.taskmaster.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndOwner(Long id, User owner);

    // Find projects where a specific user is a member
    Page<Project> findByMembersContaining(User member, Pageable pageable);

    // Find projects owned by a specific user
    Page<Project> findByOwner(User owner, Pageable pageable);

    // Check if a user is a member of a specific project
    boolean existsByIdAndMembersContaining(Long projectId, User member);

    boolean existsByIdAndOwner(Long projectId, User owner);

}