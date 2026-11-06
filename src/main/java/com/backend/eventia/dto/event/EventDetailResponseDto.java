package com.backend.eventia.dto.event;

import com.backend.eventia.model.enums.EventStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record EventDetailResponseDto(
        Long id,
        String title,
        String description,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String location,
        Integer capacity,
        EventStatus status,
        boolean active,
        Long categoryId,
        String categoryName,
        Long organizerId,
        String organizerName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
