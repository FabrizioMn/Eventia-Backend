package com.backend.eventia.repository;

import com.backend.eventia.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // ───────────────────────────── Admin Module ─────────────────────────────

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Ticket> findAllByOrderByCreatedAtAsc();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.orderDetail.ticketType.event.id = :eventId")
    long countTicketsByEventId(@Param("eventId") Long eventId);
}
