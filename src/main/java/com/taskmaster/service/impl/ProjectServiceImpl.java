package com.taskmaster.service.impl;

import com.taskmaster.dto.PagedResponse;
import com.taskmaster.dto.project.ProjectCreateRequest;
import com.taskmaster.dto.project.ProjectResponse;
import com.taskmaster.dto.user.UserSummaryResponse;
import com.taskmaster.exception.BadRequestException;
import com.taskmaster.exception.ResourceNotFoundException;
import com.taskmaster.model.Project;
import com.taskmaster.model.User;
import com.taskmaster.repository.ProjectRepository;
import com.taskmaster.repository.UserRepository;
import com.taskmaster.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Autowired private ProjectRepository projectRepository;
    @Autowired private UserRepository userRepository;
    // Inject TaskService if needed for cascading deletes or counts

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest createRequest, Long ownerUserId) {
        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", ownerUserId));

        Project project = Project.builder()
                .name(createRequest.getName())
                .description(createRequest.getDescription())
                .owner(owner)
                .build();

        // Automatically add the owner as a member
        project.addMember(owner);

        Project savedProject = projectRepository.save(project);
        logger.info("User {} created project {}", ownerUserId, savedProject.getId());
        return mapToProjectResponse(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, Long currentUserId) {
        verifyUserMembership(projectId, currentUserId); // Authorization check
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        return mapToProjectResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProjectResponse> getProjectsForUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Page<Project> projectsPage = projectRepository.findByMembersContaining(user, pageable);

        List<ProjectResponse> projectResponses = projectsPage.getContent().stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                projectResponses,
                projectsPage.getNumber(),
                projectsPage.getSize(),
                projectsPage.getTotalElements(),
                projectsPage.getTotalPages(),
                projectsPage.isLast()
        );
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectCreateRequest updateRequest, Long currentUserId) {
        verifyProjectOwner(projectId, currentUserId); // Authorization check: Only owner can update

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        project.setName(updateRequest.getName());
        project.setDescription(updateRequest.getDescription());

        Project updatedProject = projectRepository.save(project);
        logger.info("User {} updated project {}", currentUserId, projectId);
        return mapToProjectResponse(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, Long currentUserId) {
        verifyProjectOwner(projectId, currentUserId); // Authorization check: Only owner can delete

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // Cascading should handle tasks, comments, attachments via annotations in Project/Task models
        // If not using CascadeType.ALL/orphanRemoval=true, manual deletion is needed here.
        // Also, if using external file storage, files might need manual deletion.

        projectRepository.delete(project);
        logger.info("User {} deleted project {}", currentUserId, projectId);
    }

    @Override
    @Transactional
    public void addMemberToProject(Long projectId, Long userIdToAdd, Long currentUserId) {
        verifyProjectOwner(projectId, currentUserId); // Authorization: Only owner can add members (adjust if needed)

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        User userToAdd = userRepository.findById(userIdToAdd)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userIdToAdd));

        if (project.getMembers().contains(userToAdd)) {
            throw new BadRequestException("User is already a member of this project.");
        }

        project.addMember(userToAdd);
        projectRepository.save(project); // Need to save project to persist membership change
        logger.info("User {} added user {} to project {}", currentUserId, userIdToAdd, projectId);
    }

    @Override
    @Transactional
    public void removeMemberFromProject(Long projectId, Long userIdToRemove, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        User userToRemove = userRepository.findById(userIdToRemove)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userIdToRemove));

        // Authorization: Owner can remove anyone (except maybe self), or user can remove self.
        boolean isOwner = project.getOwner().getId().equals(currentUserId);
        boolean isRemovingSelf = userIdToRemove.equals(currentUserId);

        if (!isOwner && !isRemovingSelf) {
            throw new AccessDeniedException("Only the project owner can remove other members.");
        }
        if (isOwner && isRemovingSelf) {
            throw new BadRequestException("Project owner cannot remove themselves from the project."); // Or implement transfer ownership logic
        }
        if (!project.getMembers().contains(userToRemove)) {
            throw new BadRequestException("User is not a member of this project.");
        }

        project.removeMember(userToRemove);
        projectRepository.save(project);
        logger.info("User {} removed user {} from project {}", currentUserId, userIdToRemove, projectId);
    }

    // --- Authorization Helpers ---
    private void verifyUserMembership(Long projectId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (!projectRepository.existsByIdAndMembersContaining(projectId, user)) {
            logger.warn("Access denied: User {} is not a member of project {}", userId, projectId);
            throw new AccessDeniedException("User is not a member of project " + projectId);
        }
    }

    private void verifyProjectOwner(Long projectId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (!projectRepository.existsByIdAndOwner(projectId, user)) {
            logger.warn("Access denied: User {} is not the owner of project {}", userId, projectId);
            throw new AccessDeniedException("User is not the owner of project " + projectId);
        }
    }

    // --- Mapping ---
    private ProjectResponse mapToProjectResponse(Project project) {
        ProjectResponse dto = new ProjectResponse();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        if (project.getOwner() != null) {
            dto.setOwner(new UserSummaryResponse(
                    project.getOwner().getId(),
                    project.getOwner().getUsername(),
                    project.getOwner().getFirstName(),
                    project.getOwner().getLastName()
            ));
        }
        // Add counts here if desired
        return dto;
    }
}