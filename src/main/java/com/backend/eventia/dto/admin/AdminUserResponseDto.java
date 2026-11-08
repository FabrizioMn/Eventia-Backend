package com.backend.eventia.dto.admin;

import com.backend.eventia.model.enums.UserRole;

import java.time.LocalDateTime;

public record AdminUserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String dni,
        String email,
        String cellphone,
        boolean active,
        UserRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

