package com.backend.eventia.dto.organizer;

public record ActiveShiftDto(
        String venue,
        String gateSystemStatus,
        String generalCapacityWarning
) {
}

