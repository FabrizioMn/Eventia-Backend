package com.backend.eventia.dto.organizer;

public record ZoneDistributionDataPointDto(
        String zoneName,
        long soldCount,
        long totalCapacity,
        int percentage,
        String color
) {
}

