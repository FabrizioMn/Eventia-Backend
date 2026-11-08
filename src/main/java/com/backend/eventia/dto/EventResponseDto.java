package com.backend.eventia.dto;

import com.backend.eventia.model.enums.EventStatus;
import com.backend.eventia.model.enums.InactivationReason;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record EventResponseDto(
        String id,
        String title,
        String description,
        LocalDate date,
        @JsonProperty("start_time") LocalTime startTime,
        @JsonProperty("end_time") LocalTime endTime,
        String location,
        String city,
        Integer capacity,
        @JsonProperty("available_capacity") Integer availableCapacity,
        EventStatus status,
        boolean active,
        @JsonProperty("inactivation_reason") InactivationReason inactivationReason,
        @JsonProperty("id_category") String categoryId,
        @JsonProperty("category_name") String categoryName,
        @JsonProperty("id_organizer") Long organizerId,
        @JsonProperty("organizer_name") String organizerName,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt
) {
}
