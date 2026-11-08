package com.backend.eventia.dto.admin;

public record KpiMetricDto(
        double value,
        double percentageChange,
        double previousMonthValue
) {
}

