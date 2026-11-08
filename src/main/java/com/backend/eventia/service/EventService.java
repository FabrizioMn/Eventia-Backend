package com.backend.eventia.service;

import com.backend.eventia.dto.EventInactivateRequestDto;
import com.backend.eventia.dto.EventRequestDto;
import com.backend.eventia.dto.EventResponseDto;

import java.util.List;

public interface EventService {
    EventResponseDto create(EventRequestDto request, String organizerEmail);
    List<EventResponseDto> listPublic();
    List<EventResponseDto> listByOrganizer(String organizerEmail);
    List<EventResponseDto> listAll();
    EventResponseDto getById(Long id);
    EventResponseDto getPublicById(Long id);
    EventResponseDto update(Long id, EventRequestDto request, String actorEmail);
    EventResponseDto inactivate(Long id, EventInactivateRequestDto request, String actorEmail);
    void softDelete(Long id, String actorEmail);
}
