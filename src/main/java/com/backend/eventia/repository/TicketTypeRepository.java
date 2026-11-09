package com.backend.eventia.repository;

import com.backend.eventia.model.TicketType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    List<TicketType> findByEventId(Long eventId);

    List<TicketType> findByEventIdAndActiveTrue(Long eventId);

    boolean existsByNameIgnoreCaseAndEventId(String name, Long eventId);

    @Query("SELECT t FROM TicketType t WHERE t.event.organizer.id = :organizerId AND t.active = true")
    List<TicketType> findByOrganizerIdAndActiveTrue(@Param("organizerId") Long organizerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketType t WHERE t.id = :id")
    Optional<TicketType> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT COALESCE(SUM(t.totalCapacity), 0) FROM TicketType t
            WHERE t.event.id = :eventId
              AND (t.active = true OR t.availableCapacity < t.totalCapacity)
            """)
    Integer sumCapacityByEventId(@Param("eventId") Long eventId);

    @Query("""
            SELECT COALESCE(SUM(t.totalCapacity), 0) FROM TicketType t
            WHERE t.event.id = :eventId
              AND t.id <> :excludeId
              AND (t.active = true OR t.availableCapacity < t.totalCapacity)
            """)
    Integer sumCapacityByEventIdExcluding(
            @Param("eventId") Long eventId,
            @Param("excludeId") Long excludeId
    );
}
