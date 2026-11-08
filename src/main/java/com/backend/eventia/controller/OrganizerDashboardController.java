package com.backend.eventia.controller;

import com.backend.eventia.dto.organizer.OrganizerDashboardResponseDto;
import com.backend.eventia.service.OrganizerDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/organizer/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerDashboardController {

    private final OrganizerDashboardService organizerDashboardService;

    @GetMapping
    public ResponseEntity<OrganizerDashboardResponseDto> getDashboardData(Authentication authentication) {
        log.info("[OrganizerDashboardController] GET /api/organizer/dashboard - Consultando métricas para usuario='{}'",
                authentication.getName());

        OrganizerDashboardResponseDto data = organizerDashboardService.getDashboardData(authentication.getName());
        return ResponseEntity.ok(data);
    }
}

