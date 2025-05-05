package com.taskmaster.controller;

import com.taskmaster.dto.user.UserResponse;
import com.taskmaster.dto.user.UserUpdateRequest;
import com.taskmaster.security.UserPrincipal;
import com.taskmaster.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUserProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserResponse userProfile = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateCurrentUserProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody UserUpdateRequest updateRequest) {
        UserResponse updatedUser = userService.updateUserProfile(currentUser.getId(), updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    // Add endpoints for changing password if implementing that feature
    // @PostMapping("/me/password")
    // public ResponseEntity<?> changePassword(...)
}