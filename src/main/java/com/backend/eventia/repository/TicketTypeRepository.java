package com.backend.eventia.repository;

import com.backend.eventia.model.TicketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    List<TicketType> findByEventId(Long eventId);

    List<TicketType> findByEventIdAndActiveTrue(Long eventId);

    Page<TicketType> findByEventId(Long eventId, Pageable pageable);

    Page<TicketType> findByEventIdAndActiveTrue(Long eventId, Pageable pageable);

    boolean existsByNameIgnoreCaseAndEventId(String name, Long eventId);

    @Query("SELECT t FROM TicketType t WHERE t.event.organizer.id = :organizerId AND t.active = true")
    List<TicketType> findByOrganizerIdAndActiveTrue(@Param("organizerId") Long organizerId);

    @Query("SELECT COALESCE(SUM(t.totalCapacity), 0) FROM TicketType t WHERE t.event.id = :eventId AND t.active = true")
    Integer sumCapacityByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COALESCE(SUM(t.totalCapacity), 0) FROM TicketType t WHERE t.event.id = :eventId AND t.id <> :excludeId AND t.active = true")
    Integer sumCapacityByEventIdExcluding(@Param("eventId") Long eventId, @Param("excludeId") Long excludeId);
}
