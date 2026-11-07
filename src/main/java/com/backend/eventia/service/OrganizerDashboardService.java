package com.backend.eventia.service;

import com.backend.eventia.dto.organizer.OrganizerDashboardResponseDto;

public interface OrganizerDashboardService {

    OrganizerDashboardResponseDto getDashboardData(String organizerEmail);
}

