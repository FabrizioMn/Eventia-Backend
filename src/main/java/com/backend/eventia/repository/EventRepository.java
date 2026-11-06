package com.backend.eventia.repository;

import com.backend.eventia.model.Event;
import com.backend.eventia.model.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByOrganizerId(Long organizerId, Pageable pageable);

    Page<Event> findByOrganizerIdAndActiveTrue(Long organizerId, Pageable pageable);

    Page<Event> findByActiveTrue(Pageable pageable);

    Page<Event> findByActiveTrueAndStatus(EventStatus status, Pageable pageable);

    boolean existsByTitleIgnoreCaseAndOrganizerId(String title, Long organizerId);
}
