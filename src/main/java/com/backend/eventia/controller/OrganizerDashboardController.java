package com.backend.eventia.controller;

import com.backend.eventia.dto.organizer.OrganizerDashboardResponseDto;
import com.backend.eventia.dto.organizer.OrganizerLiveSaleNotificationDto;
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

    @PostMapping("/simulate-sale")
    public ResponseEntity<OrganizerLiveSaleNotificationDto> simulateSale(
            @RequestParam(required = false, defaultValue = "Lima Live Sessions") String eventTitle,
            @RequestParam(required = false, defaultValue = "Campo VIP Platinum") String ticketTypeName,
            @RequestParam(required = false, defaultValue = "2") Integer quantity,
            @RequestParam(required = false, defaultValue = "180.0") Double unitPrice,
            @RequestParam(required = false, defaultValue = "Cliente Demo") String buyerName,
            Authentication authentication
    ) {
        log.info("[OrganizerDashboardController] POST /api/organizer/dashboard/simulate-sale para usuario='{}'",
                authentication.getName());

        OrganizerLiveSaleNotificationDto notification = organizerDashboardService.broadcastSaleNotification(
                authentication.getName(),
                eventTitle,
                ticketTypeName,
                quantity,
                unitPrice,
                buyerName
        );
        return ResponseEntity.ok(notification);
    }
}

