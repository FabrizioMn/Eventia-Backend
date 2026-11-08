package com.backend.eventia.repository;

import com.backend.eventia.model.Event;
import com.backend.eventia.model.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByActiveTrueAndStatus(EventStatus status);
    List<Event> findByActiveTrue();
    List<Event> findByOrganizerId(Long organizerId);
    Optional<Event> findByIdAndActiveTrue(Long id);
}
