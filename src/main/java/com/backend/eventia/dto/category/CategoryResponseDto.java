package com.backend.eventia.dto.category;

import java.time.LocalDateTime;

public record CategoryResponseDto(
        Long id,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
