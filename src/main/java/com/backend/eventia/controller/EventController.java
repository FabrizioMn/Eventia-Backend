package com.backend.eventia.controller;

import com.backend.eventia.dto.EventResponseDto;
import com.backend.eventia.model.Event;
import com.backend.eventia.model.TicketType;
import com.backend.eventia.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;

    // GET /events -> lista sencilla para el catálogo del frontend
    @GetMapping
    public List<EventResponseDto> listEvents() {
        log.info("[EventController] GET /events recibido");
        List<EventResponseDto> result = eventRepository.findAll().stream()
                .filter(Event::isActive)
                .map(this::toDto)
                .toList();
        log.info("[EventController] Se devolvieron {} eventos", result.size());
        return result;
    }

    private EventResponseDto toDto(Event event) {
        // Precio más barato y % vendido desde los tipos de entrada
        List<TicketType> tickets = event.getTicketTypes() == null ? List.of() : event.getTicketTypes();
        Double price = tickets.stream()
                .map(TicketType::getPrice)
                .min(Double::compareTo)
                .orElse(0.0);

        int total = tickets.stream().mapToInt(TicketType::getTotalCapacity).sum();
        int available = tickets.stream().mapToInt(TicketType::getAvailableCapacity).sum();
        int soldPct = total > 0 ? Math.round((float) (total - available) / total * 100) : 0;

        return new EventResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getLocation(),
                event.getCapacity(),
                event.getStatus(),
                event.getCategory() == null ? "" : event.getCategory().getName(),
                price,
                soldPct
        );
    }
}
