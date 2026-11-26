package com.backend.eventia.dto.event;

import com.backend.eventia.model.enums.EventStatus;

import java.time.LocalDateTime;

public record EventNotificationDto(
        Long eventId,
        String title,
        String organizerName,
        String categoryName,
        EventStatus status,
        String message,
        LocalDateTime timestamp,
        EventSummaryResponseDto event
) {
}
