package com.backend.eventia.repository;

import com.backend.eventia.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

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
