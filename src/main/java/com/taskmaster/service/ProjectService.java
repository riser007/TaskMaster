package com.taskmaster.service;

import com.taskmaster.dto.PagedResponse;
import com.taskmaster.dto.project.ProjectCreateRequest;
import com.taskmaster.dto.project.ProjectResponse;
import org.springframework.data.domain.Pageable;

public interface ProjectService {

    ProjectResponse createProject(ProjectCreateRequest createRequest, Long ownerUserId);

    ProjectResponse getProjectById(Long projectId, Long currentUserId);

    PagedResponse<ProjectResponse> getProjectsForUser(Long userId, Pageable pageable);

    ProjectResponse updateProject(Long projectId, ProjectCreateRequest updateRequest, Long currentUserId);

    void deleteProject(Long projectId, Long currentUserId);

    void addMemberToProject(Long projectId, Long userIdToAdd, Long currentUserId);

    void removeMemberFromProject(Long projectId, Long userIdToRemove, Long currentUserId);
}