package com.backend.eventia.service;

import com.backend.eventia.dto.EventResponseDto;

import java.util.List;

public interface EventService {

    List<EventResponseDto> findAllActive();
}
