package com.backend.eventia.dto.admin;

import com.backend.eventia.model.enums.EventStatus;

public record ActiveEventResponseDto(
        Long id,
        String title,
        String categoryName,
        String organizerName,
        int ticketsSold,
        int capacity,
        EventStatus status
) {
}

