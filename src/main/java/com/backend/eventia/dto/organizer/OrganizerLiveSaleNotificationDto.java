package com.backend.eventia.dto.organizer;

import java.time.LocalDateTime;

public record OrganizerLiveSaleNotificationDto(
        Long organizerId,
        String eventTitle,
        String ticketTypeName,
        Integer quantity,
        Double unitPrice,
        Double subtotal,
        String buyerName,
        String message,
        LocalDateTime timestamp,
        OrganizerDashboardResponseDto updatedDashboard
) {
}

