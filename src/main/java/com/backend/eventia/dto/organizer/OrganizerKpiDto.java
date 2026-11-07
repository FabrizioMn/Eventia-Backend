package com.backend.eventia.dto.organizer;

public record OrganizerKpiDto(
        long activeEvents,
        Double activeEventsChange,
        long ticketsSold,
        Double ticketsSoldChange,
        double totalRevenuePEN,
        Double totalRevenueChange,
        int occupancyRate,
        Double occupancyRateChange
) {
}

