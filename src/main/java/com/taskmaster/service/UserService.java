package com.taskmaster.service;

import com.taskmaster.dto.user.UserResponse;
import com.taskmaster.dto.user.UserUpdateRequest;

public interface UserService {
    UserResponse getUserProfile(Long userId);
    UserResponse updateUserProfile(Long userId, UserUpdateRequest updateRequest);

}