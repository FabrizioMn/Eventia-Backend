package com.backend.eventia.service;

import com.backend.eventia.dto.EventResponseDto;
import com.backend.eventia.dto.event.EventDetailResponseDto;
import com.backend.eventia.dto.event.EventRequestDto;
import com.backend.eventia.dto.event.EventSummaryResponseDto;
import com.backend.eventia.model.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventService {

    List<EventResponseDto> findAllActive();

    Page<EventSummaryResponseDto> findAll(boolean includeInactive, Pageable pageable);

    Page<EventSummaryResponseDto> findByOrganizer(String organizerEmail, boolean includeInactive, Pageable pageable);

    EventDetailResponseDto findById(Long id);

    EventDetailResponseDto create(EventRequestDto request, String organizerEmail);

    EventDetailResponseDto update(Long id, EventRequestDto request, String userEmail);

    void updateStatus(Long id, EventStatus status, String userEmail);

    void updateActive(Long id, boolean active, String userEmail);
}