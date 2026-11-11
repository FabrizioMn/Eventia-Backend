package com.backend.eventia.controller;

import com.backend.eventia.dto.event.EventSummaryResponseDto;
import com.backend.eventia.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<EventSummaryResponseDto>> findAll(
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("[AdminEventController] GET /api/admin/events - page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        return ResponseEntity.ok(eventService.findAll(true, pageable));
    }
}
