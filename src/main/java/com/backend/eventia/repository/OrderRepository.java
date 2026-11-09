package com.backend.eventia.repository;

import com.backend.eventia.model.Order;
import com.backend.eventia.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Consultas del modulo Admin

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = com.backend.eventia.model.enums.OrderStatus.PAID")
    double sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = com.backend.eventia.model.enums.OrderStatus.PAID AND o.createdAt >= :start AND o.createdAt < :end")
    double sumTotalRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByPaymentStatus(OrderStatus paymentStatus);

    long countByPaymentStatusAndCreatedAtBetween(OrderStatus paymentStatus, LocalDateTime start, LocalDateTime end);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Order> findAllByOrderByCreatedAtAsc();
}
