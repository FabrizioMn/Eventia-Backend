package com.backend.eventia.controller;

import com.backend.eventia.dto.TicketTypeResponseDto;
import com.backend.eventia.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CatalogTicketTypeController {

    private final TicketTypeRepository ticketTypeRepository;

    @GetMapping({"/ticket-types", "/ticket_types"})
    public ResponseEntity<List<TicketTypeResponseDto>> list(@RequestParam(name = "id_event", required = false) Long eventId) {
        var items = eventId != null ? ticketTypeRepository.findByEventId(eventId) : ticketTypeRepository.findAll();
        List<TicketTypeResponseDto> result = items.stream()
                .filter(t -> t.isActive())
                .map(t -> new TicketTypeResponseDto(
                        t.getId() != null ? String.valueOf(t.getId()) : null,
                        t.getEvent() != null ? t.getEvent().getId() : null,
                        t.getName(),
                        t.getPrice(),
                        t.getAvailableCapacity()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }
}
