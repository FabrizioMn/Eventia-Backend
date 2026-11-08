package com.backend.eventia.dto.admin;

public record RevenueTimelineResponseDto(
        String period,
        double revenue,
        long ordersCount
) {
}

