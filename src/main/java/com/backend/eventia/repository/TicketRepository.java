package com.backend.eventia.repository;

import com.backend.eventia.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Consultas del modulo Admin

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Ticket> findAllByOrderByCreatedAtAsc();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.orderDetail.ticketType.event.id = :eventId")
    long countTicketsByEventId(@Param("eventId") Long eventId);

    @Query("""
            SELECT t FROM Ticket t
            JOIN FETCH t.orderDetail od
            JOIN FETCH od.order o
            JOIN FETCH od.ticketType tt
            JOIN FETCH tt.event e
            WHERE o.user.id = :userId
            ORDER BY e.date ASC, e.startTime ASC
            """)
    List<Ticket> findByUserId(@Param("userId") Long userId);
}
