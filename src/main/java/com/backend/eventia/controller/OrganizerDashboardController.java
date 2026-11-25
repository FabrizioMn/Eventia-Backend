package com.backend.eventia.controller;

import com.backend.eventia.dto.organizer.OrganizerDashboardResponseDto;
import com.backend.eventia.service.OrganizerDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/organizer/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
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
