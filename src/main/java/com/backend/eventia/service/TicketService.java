package com.backend.eventia.service;

import com.backend.eventia.dto.order.OrderRequestDto;
import com.backend.eventia.dto.order.OrderResponseDto;
import com.backend.eventia.dto.ticket.TicketResponseDto;

import java.util.List;

public interface TicketService {

    OrderResponseDto purchase(OrderRequestDto requestDto, String userEmail);

    List<TicketResponseDto> findMyTickets(String userEmail);
}
