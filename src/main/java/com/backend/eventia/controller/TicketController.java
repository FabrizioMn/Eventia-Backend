package com.backend.eventia.controller;

import com.backend.eventia.dto.order.OrderRequestDto;
import com.backend.eventia.dto.order.OrderResponseDto;
import com.backend.eventia.dto.ticket.TicketResponseDto;
import com.backend.eventia.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/api/orders")
    public ResponseEntity<OrderResponseDto> purchase(@RequestBody @Valid OrderRequestDto request, Authentication authentication) {
        log.info("[TicketController] POST /api/orders - usuario='{}'", authentication.getName());

        OrderResponseDto response = ticketService.purchase(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/tickets/my")
    public ResponseEntity<List<TicketResponseDto>> findMyTickets(Authentication authentication) {
        log.debug("[TicketController] GET /api/tickets/my - usuario='{}'", authentication.getName());

        return ResponseEntity.ok(ticketService.findMyTickets(authentication.getName()));
    }
}
