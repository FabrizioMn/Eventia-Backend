package com.backend.eventia.dto.organizer;

import com.backend.eventia.dto.event.EventSummaryResponseDto;

import java.util.List;

public record OrganizerDashboardResponseDto(
        OrganizerKpiDto kpis,
        List<DailySalesDataPointDto> dailySales,
        List<ZoneDistributionDataPointDto> zoneDistribution,
        List<EventSummaryResponseDto> recentEvents,
        ActiveShiftDto activeShift
) {
}

