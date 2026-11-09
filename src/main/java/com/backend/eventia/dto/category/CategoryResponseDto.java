package com.backend.eventia.dto.category;

public record CategoryResponseDto(
        Long id,
        String name,
        String description,
        boolean active
) {
}
