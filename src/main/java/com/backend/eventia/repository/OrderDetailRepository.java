package com.backend.eventia.repository;

import com.backend.eventia.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    boolean existsByTicketTypeId(Long ticketTypeId);

    @Query("SELECT COALESCE(SUM(od.subtotal), 0.0) FROM OrderDetail od " +
            "WHERE od.ticketType.event.organizer.id = :organizerId " +
            "AND od.order.paymentStatus = com.backend.eventia.model.enums.OrderStatus.PAID")
    Double sumRevenueByOrganizerId(@Param("organizerId") Long organizerId);

    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od " +
            "WHERE od.ticketType.event.organizer.id = :organizerId " +
            "AND od.order.paymentStatus = com.backend.eventia.model.enums.OrderStatus.PAID")
    Integer sumTicketsSoldByOrganizerId(@Param("organizerId") Long organizerId);

    @Query("SELECT od FROM OrderDetail od " +
            "WHERE od.ticketType.event.organizer.id = :organizerId " +
            "AND od.order.paymentStatus = com.backend.eventia.model.enums.OrderStatus.PAID")
    List<OrderDetail> findByOrganizerId(@Param("organizerId") Long organizerId);
}
