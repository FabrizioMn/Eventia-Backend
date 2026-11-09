package com.backend.eventia.dto.order;

import com.backend.eventia.dto.ticket.TicketResponseDto;
import com.backend.eventia.model.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
        Long id,
        Double totalAmount,
        Double discountAmount,
        String paymentMethod,
        OrderStatus status,
        LocalDateTime createdAt,
        List<Detail> details
) {
    public record Detail(
            Long ticketTypeId,
            String ticketTypeName,
            Integer quantity,
            Double unitPrice,
            Double subtotal,
            Double discountApplied,
            List<TicketResponseDto> tickets
    ) {
    }
}
