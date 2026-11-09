package com.backend.eventia.dto.admin;

import java.util.List;

public record MonitoringDashboardResponseDto(
        DashboardKpiResponseDto kpis,
        List<RevenueTimelineResponseDto> revenueTimeline,
        List<TicketFlowResponseDto> ticketFlow,
        List<ActiveEventResponseDto> activeEvents
) {
}

