package com.backend.eventia.controller;

import com.backend.eventia.dto.EventInactivateRequestDto;
import com.backend.eventia.dto.EventRequestDto;
import com.backend.eventia.dto.EventResponseDto;
import com.backend.eventia.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventResponseDto>> listPublic() {
        return ResponseEntity.ok(eventService.listPublic());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getPublicById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getPublicById(id));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<List<EventResponseDto>> listMine(Authentication authentication) {
        return ResponseEntity.ok(eventService.listByOrganizer(authentication.getName()));
    }

    @GetMapping("/manage")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventResponseDto>> listAll() {
        return ResponseEntity.ok(eventService.listAll());
    }

    @GetMapping("/manage/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<EventResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<EventResponseDto> create(@Valid @RequestBody EventRequestDto request,
                                                   Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.create(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<EventResponseDto> update(@PathVariable Long id,
                                                   @Valid @RequestBody EventRequestDto request,
                                                   Authentication authentication) {
        return ResponseEntity.ok(eventService.update(id, request, authentication.getName()));
    }

    @PatchMapping("/{id}/inactivate")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<EventResponseDto> inactivate(@PathVariable Long id,
                                                       @Valid @RequestBody EventInactivateRequestDto request,
                                                       Authentication authentication) {
        return ResponseEntity.ok(eventService.inactivate(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<Void> softDelete(@PathVariable Long id, Authentication authentication) {
        eventService.softDelete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
