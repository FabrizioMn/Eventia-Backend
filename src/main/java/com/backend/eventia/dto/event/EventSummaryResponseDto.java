package com.backend.eventia.dto.event;

import com.backend.eventia.model.enums.EventStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventSummaryResponseDto(
        Long id,
        String title,
        String description,
        LocalDate date,
        LocalTime startTime,
        String location,
        String categoryName,
        EventStatus status
) {
}
