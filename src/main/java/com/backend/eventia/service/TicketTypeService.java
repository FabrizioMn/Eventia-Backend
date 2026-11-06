package com.backend.eventia.service;

import com.backend.eventia.dto.tickettype.TicketTypeRequestDto;
import com.backend.eventia.dto.tickettype.TicketTypeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketTypeService {

    List<TicketTypeResponseDto> findByEvent(Long eventId, boolean includeInactive);

    Page<TicketTypeResponseDto> findByEvent(Long eventId, boolean includeInactive, Pageable pageable);

    TicketTypeResponseDto findById(Long id);

    TicketTypeResponseDto create(Long eventId, TicketTypeRequestDto request, String userEmail);

    TicketTypeResponseDto update(Long id, TicketTypeRequestDto request, String userEmail);

    void updateStatus(Long id, boolean active, String userEmail);

    void delete(Long id, String userEmail);
}

