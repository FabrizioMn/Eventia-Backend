package com.backend.eventia.repository;

import com.backend.eventia.model.Event;
import com.backend.eventia.model.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrganizerId(Long organizerId);

    List<Event> findByOrganizerIdAndActiveTrue(Long organizerId);

    long countByOrganizerIdAndActiveTrue(Long organizerId);

    Page<Event> findByOrganizerId(Long organizerId, Pageable pageable);

    Page<Event> findByOrganizerIdAndActiveTrue(Long organizerId, Pageable pageable);

    Page<Event> findByActiveTrue(Pageable pageable);

    Page<Event> findByActiveTrueAndStatus(EventStatus status, Pageable pageable);

    boolean existsByTitleIgnoreCaseAndOrganizerId(String title, Long organizerId);
}
