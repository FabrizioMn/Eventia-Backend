package com.backend.eventia.dto.organizer;

public record DailySalesDataPointDto(
        String day,
        String date,
        double totalPEN,
        long ticketsSold
) {
}

