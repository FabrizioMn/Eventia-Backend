package com.backend.eventia.dto.admin;

import java.time.LocalDateTime;

public record AdminCategoryResponseDto(
        Long id,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

