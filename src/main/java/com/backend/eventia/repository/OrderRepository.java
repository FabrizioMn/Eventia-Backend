package com.backend.eventia.repository;

import com.backend.eventia.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Consultas del modulo Admin

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = 'PAGADO' OR o.paymentStatus = 'COMPLETED'")
    double sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE (o.paymentStatus = 'PAGADO' OR o.paymentStatus = 'COMPLETED') AND o.createdAt >= :start AND o.createdAt < :end")
    double sumTotalRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = 'PAGADO' OR o.paymentStatus = 'COMPLETED'")
    long countPaidOrders();

    @Query("SELECT COUNT(o) FROM Order o WHERE (o.paymentStatus = 'PAGADO' OR o.paymentStatus = 'COMPLETED') AND o.createdAt >= :start AND o.createdAt < :end")
    long countPaidOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Order> findAllByOrderByCreatedAtAsc();
}
