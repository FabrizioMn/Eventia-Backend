package com.backend.eventia.dto.admin;

public record DashboardKpiResponseDto(
        KpiMetricDto totalRevenue,
        KpiMetricDto processedOrders,
        KpiMetricDto issuedTickets,
        KpiMetricDto activeOrganizers
) {
}

