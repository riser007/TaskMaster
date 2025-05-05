package com.taskmaster.service;

import com.taskmaster.dto.auth.RegisterRequest;

public interface AuthService {
    String authenticateUser(LoginRequest loginRequest);
    void registerUser(RegisterRequest registerRequest);
}