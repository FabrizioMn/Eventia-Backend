package com.backend.eventia.dto.admin;

public record ActiveEventResponseDto(
        Long id,
        String title,
        String categoryName,
        String organizerName,
        int ticketsSold,
        int capacity,
        String status
) {
}

