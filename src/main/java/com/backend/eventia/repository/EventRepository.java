package com.backend.eventia.repository;

import com.backend.eventia.model.Event;
import com.backend.eventia.model.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // ───────────────────────────── Admin Module ─────────────────────────────

    @Query("""
            SELECT DISTINCT e FROM Event e
            JOIN FETCH e.category
            JOIN FETCH e.organizer
            LEFT JOIN FETCH e.ticketTypes
            WHERE e.active = true
            ORDER BY e.date ASC, e.startTime ASC
            """)
    List<Event> findActiveEventsWithDetails();
    List<Event> findByOrganizerId(Long organizerId);

    List<Event> findByOrganizerIdAndActiveTrue(Long organizerId);

    long countByOrganizerIdAndActiveTrue(Long organizerId);

    Page<Event> findByOrganizerId(Long organizerId, Pageable pageable);

    Page<Event> findByOrganizerIdAndActiveTrue(Long organizerId, Pageable pageable);

    Page<Event> findByActiveTrue(Pageable pageable);

    Page<Event> findByActiveTrueAndStatus(EventStatus status, Pageable pageable);

    boolean existsByTitleIgnoreCaseAndOrganizerId(String title, Long organizerId);
}
