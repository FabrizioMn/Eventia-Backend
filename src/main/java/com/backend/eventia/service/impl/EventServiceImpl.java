package com.backend.eventia.service.impl;

import com.backend.eventia.dto.EventResponseDto;
import com.backend.eventia.model.Event;
import com.backend.eventia.model.TicketType;
import com.backend.eventia.repository.EventRepository;
import com.backend.eventia.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDto> findAllActive() {

        List<EventResponseDto> events = eventRepository.findAll().stream()
                .filter(Event::isActive)
                .map(this::toDto)
                .toList();

        log.debug("[Event] findAllActive -> {} eventos", events.size());
        return events;
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
