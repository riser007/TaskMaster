package com.taskmaster.controller;

import com.taskmaster.dto.PagedResponse;
import com.taskmaster.dto.project.AddMemberRequest;
import com.taskmaster.dto.project.ProjectCreateRequest;
import com.taskmaster.dto.project.ProjectResponse;
import com.taskmaster.security.UserPrincipal;
import com.taskmaster.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectCreateRequest createRequest,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ProjectResponse project = projectService.createProject(createRequest, currentUser.getId());
        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<ProjectResponse>> getMyProjects(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        // TODO: Validate sortBy field
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PagedResponse<ProjectResponse> projects = projectService.getProjectsForUser(currentUser.getId(), pageable);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ProjectResponse project = projectService.getProjectById(projectId, currentUser.getId());
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectCreateRequest updateRequest,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ProjectResponse updatedProject = projectService.updateProject(projectId, updateRequest, currentUser.getId());
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        projectService.deleteProject(projectId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // --- Member Management ---

    @PostMapping("/{projectId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody AddMemberRequest addMemberRequest,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        projectService.addMemberToProject(projectId, addMemberRequest.getUserId(), currentUser.getId());
        return ResponseEntity.ok("Member added successfully."); // Or return updated member list?
    }

    @DeleteMapping("/{projectId}/members/{userIdToRemove}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userIdToRemove,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        projectService.removeMemberFromProject(projectId, userIdToRemove, currentUser.getId());
        return ResponseEntity.ok("Member removed successfully."); // Or use noContent()
    }
}