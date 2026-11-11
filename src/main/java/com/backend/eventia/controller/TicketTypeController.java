package com.backend.eventia.controller;

import com.backend.eventia.dto.tickettype.TicketTypeRequestDto;
import com.backend.eventia.dto.tickettype.TicketTypeResponseDto;
import com.backend.eventia.service.TicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @GetMapping("/api/events/{eventId}/ticket-types")
    public ResponseEntity<List<TicketTypeResponseDto>> findByEvent(
            @PathVariable Long eventId,
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        log.debug("[TicketTypeController] GET /api/events/{}/ticket-types - Listando tarifas (includeInactive={})", eventId, includeInactive);

        List<TicketTypeResponseDto> tickets = ticketTypeService.findByEvent(eventId, includeInactive);
        return ResponseEntity.ok(tickets);
    }

    @PostMapping("/api/events/{eventId}/ticket-types")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<TicketTypeResponseDto> create(
            @PathVariable Long eventId,
            @RequestBody @Valid TicketTypeRequestDto request,
            Authentication authentication
    ) {
        log.info("[TicketTypeController] POST /api/events/{}/ticket-types - Creando tarifa nombre='{}' por usuario='{}'",
                eventId, request.name(), authentication.getName());

        TicketTypeResponseDto created = ticketTypeService.create(eventId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/api/ticket-types/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<TicketTypeResponseDto> update(
            @PathVariable Long id,
            @RequestBody @Valid TicketTypeRequestDto request,
            Authentication authentication
    ) {
        log.info("[TicketTypeController] PUT /api/ticket-types/{} - Actualizando tarifa nombre='{}' por usuario='{}'",
                id, request.name(), authentication.getName());

        TicketTypeResponseDto updated = ticketTypeService.update(id, request, authentication.getName());
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/api/ticket-types/{id}/status")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            Authentication authentication
    ) {
        log.info("[TicketTypeController] PATCH /api/ticket-types/{}/status - Cambiando estado activo a active={} por usuario='{}'",
                id, active, authentication.getName());

        ticketTypeService.updateStatus(id, active, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/ticket-types/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        log.info("[TicketTypeController] DELETE /api/ticket-types/{} - Solicitando eliminación por usuario='{}'",
                id, authentication.getName());

        ticketTypeService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}

