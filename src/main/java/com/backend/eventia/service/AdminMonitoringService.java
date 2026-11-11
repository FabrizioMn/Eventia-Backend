package com.backend.eventia.service;

import com.backend.eventia.dto.admin.ActiveEventResponseDto;
import com.backend.eventia.dto.admin.DashboardKpiResponseDto;
import com.backend.eventia.dto.admin.MonitoringDashboardResponseDto;
import com.backend.eventia.dto.admin.RevenueTimelineResponseDto;
import com.backend.eventia.dto.admin.TicketFlowResponseDto;

import java.util.List;

public interface AdminMonitoringService {

    MonitoringDashboardResponseDto getDashboardData();

    DashboardKpiResponseDto getKpis();

    List<RevenueTimelineResponseDto> getRevenueTimeline();

    List<TicketFlowResponseDto> getTicketFlow();

    List<ActiveEventResponseDto> getActiveEvents();
}

