package com.taskmaster.service.impl;

import com.taskmaster.dto.user.UserResponse;
import com.taskmaster.dto.user.UserUpdateRequest;
import com.taskmaster.exception.ResourceNotFoundException;
import com.taskmaster.model.User;
import com.taskmaster.repository.UserRepository;
import com.taskmaster.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Inject if handling password changes
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired private UserRepository userRepository;
    // @Autowired private PasswordEncoder passwordEncoder; // Needed for password updates

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserProfile(Long userId, UserUpdateRequest updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Update mutable fields
        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getEmail() != null) {
            // Optional: Add check if email already exists for another user
            // if (userRepository.existsByEmailAndIdNot(updateRequest.getEmail(), userId)) {
            //     throw new BadRequestException("Email already in use by another account.");
            // }
            user.setEmail(updateRequest.getEmail());
        }
        // TODO: Add password update logic if required (check current password, encode new one)

        User updatedUser = userRepository.save(user);
        logger.info("Updated profile for user {}", userId);
        return mapToUserResponse(updatedUser);
    }

    // --- Mapping ---
    private UserResponse mapToUserResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}