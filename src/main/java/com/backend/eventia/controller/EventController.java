package com.backend.eventia.controller;

import com.backend.eventia.dto.EventResponseDto;
import com.backend.eventia.service.EventService;
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

    private final EventService eventService;

    // GET /events -> lista sencilla para el catálogo del frontend
    @GetMapping
    public List<EventResponseDto> listEvents() {
        log.info("[EventController] GET /events recibido");
        List<EventResponseDto> result = eventService.findAllActive();
        log.info("[EventController] Se devolvieron {} eventos", result.size());
        return result;
    }
}
