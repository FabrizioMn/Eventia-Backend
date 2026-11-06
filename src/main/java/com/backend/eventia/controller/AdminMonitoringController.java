package com.backend.eventia.controller;

import com.backend.eventia.dto.admin.ActiveEventResponseDto;
import com.backend.eventia.dto.admin.DashboardKpiResponseDto;
import com.backend.eventia.dto.admin.MonitoringDashboardResponseDto;
import com.backend.eventia.dto.admin.RevenueTimelineResponseDto;
import com.backend.eventia.dto.admin.TicketFlowResponseDto;
import com.backend.eventia.service.AdminMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/monitoring")
@RequiredArgsConstructor
public class AdminMonitoringController {

    private final AdminMonitoringService adminMonitoringService;

    @GetMapping
    public ResponseEntity<MonitoringDashboardResponseDto> getDashboardData() {
        return ResponseEntity.ok(adminMonitoringService.getDashboardData());
    }

    @GetMapping("/kpis")
    public ResponseEntity<DashboardKpiResponseDto> getKpis() {
        return ResponseEntity.ok(adminMonitoringService.getKpis());
    }

    @GetMapping("/revenue-timeline")
    public ResponseEntity<List<RevenueTimelineResponseDto>> getRevenueTimeline() {
        return ResponseEntity.ok(adminMonitoringService.getRevenueTimeline());
    }

    @GetMapping("/ticket-flow")
    public ResponseEntity<List<TicketFlowResponseDto>> getTicketFlow() {
        return ResponseEntity.ok(adminMonitoringService.getTicketFlow());
    }

    @GetMapping("/active-events")
    public ResponseEntity<List<ActiveEventResponseDto>> getActiveEvents() {
        return ResponseEntity.ok(adminMonitoringService.getActiveEvents());
    }
}

