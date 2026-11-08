package com.backend.eventia.dto;

import com.backend.eventia.model.enums.EventStatus;
import com.backend.eventia.model.enums.InactivationReason;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record EventResponseDto(
        Long id,
        String title,
        String description,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String location,
        String city,
        Integer capacity,
        Integer availableCapacity,
        EventStatus status,
        boolean active,
        InactivationReason inactivationReason,
        Long categoryId,
        String categoryName,
        Long organizerId,
        String organizerName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
