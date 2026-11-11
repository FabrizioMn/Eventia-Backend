package com.backend.eventia.dto.tickettype;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TicketTypeResponseDto(
        Long id,
        Long eventId,
        String eventTitle,
        String name,
        String zone,
        Double price,
        Integer totalCapacity,
        Integer availableCapacity,
        Integer soldCount,
        String status,
        Integer maxPerPurchase,
        boolean isPresale,
        LocalDate saleStartDate,
        LocalDate saleEndDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

