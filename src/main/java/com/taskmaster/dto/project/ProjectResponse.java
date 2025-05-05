package com.taskmaster.dto.project;

import com.taskmaster.dto.user.UserSummaryResponse;
import lombok.Data;
import java.time.Instant;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private UserSummaryResponse owner;
    private Instant createdAt;
    private Instant updatedAt;

}