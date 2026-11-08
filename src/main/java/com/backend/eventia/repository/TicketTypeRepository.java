package com.backend.eventia.repository;

import com.backend.eventia.model.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findByEventId(Long eventId);
    List<TicketType> findByEventIdAndActiveTrue(Long eventId);
}

