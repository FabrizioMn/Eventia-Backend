package com.backend.eventia.dto.admin;

public record TicketFlowResponseDto(
        String period,
        long issuedTickets,
        long redeemedTickets
) {
}

