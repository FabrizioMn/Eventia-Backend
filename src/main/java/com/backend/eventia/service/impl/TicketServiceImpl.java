package com.backend.eventia.service.impl;

import com.backend.eventia.dto.order.OrderRequestDto;
import com.backend.eventia.dto.order.OrderResponseDto;
import com.backend.eventia.dto.ticket.TicketResponseDto;
import com.backend.eventia.exception.ResourceNotFoundException;
import com.backend.eventia.model.*;
import com.backend.eventia.model.enums.EventStatus;
import com.backend.eventia.model.enums.OrderStatus;
import com.backend.eventia.model.enums.TicketStatus;
import com.backend.eventia.repository.OrderRepository;
import com.backend.eventia.repository.TicketRepository;
import com.backend.eventia.repository.TicketTypeRepository;
import com.backend.eventia.repository.UserRepository;
import com.backend.eventia.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;

    private static final int MAX_TICKETS_PER_ORDER = 10;
    private static final double PROMO_DISCOUNT_PCT = 15.0;


    @Override
    @Transactional
    public OrderResponseDto purchase(OrderRequestDto request, String userEmail) {
        log.info("[Ticket] Nueva compra solicitada por usuario='{}'", userEmail);

        User user = getUserOrThrow(userEmail);
        List<OrderRequestDto.Item> items = request.items();

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una entrada");
        }

        validateNoDuplicateTicketTypes(items);

        long totalQuantity = items.stream()
                .mapToLong(OrderRequestDto.Item::quantity)
                .sum();
        if (totalQuantity > MAX_TICKETS_PER_ORDER) {
            throw new IllegalArgumentException(
                    "Solo se pueden comprar hasta " + MAX_TICKETS_PER_ORDER + " entradas por compra");
        }

        // Ordenamos por ID para bloquear siempre en el mismo orden y evitar deadlocks
        List<OrderRequestDto.Item> sortedItems = items.stream()
                .sorted(Comparator.comparing(OrderRequestDto.Item::ticketTypeId))
                .toList();

        List<TicketType> lockedTicketTypes = new ArrayList<>(sortedItems.size());
        for (OrderRequestDto.Item item : sortedItems) {
            TicketType ticketType = ticketTypeRepository.findByIdForUpdate(item.ticketTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se encontró la tarifa con el id: " + item.ticketTypeId()));

            validateTicketTypeForPurchase(ticketType, item.quantity());
            lockedTicketTypes.add(ticketType);
        }

        boolean promo = isPromoUser(user);
        double discountPct = promo ? PROMO_DISCOUNT_PCT : 0.0;

        Order order = Order.builder()
                .user(user)
                .paymentMethod(request.paymentMethod())
                .paymentStatus(OrderStatus.PAID)
                .totalAmount(0.0)
                .discountAmount(0.0)
                .build();

        double totalSubtotal = 0.0;
        double totalDiscount = 0.0;

        for (int i = 0; i < sortedItems.size(); i++) {
            OrderRequestDto.Item item = sortedItems.get(i);
            TicketType ticketType = lockedTicketTypes.get(i);

            double unitPrice = ticketType.getPrice();
            double subtotal = round2(unitPrice * item.quantity());
            double discountApplied = round2(subtotal * discountPct / 100.0);

            totalSubtotal += subtotal;
            totalDiscount += discountApplied;

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .ticketType(ticketType)
                    .quantity(item.quantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .discountApplied(discountApplied)
                    .build();

            for (int n = 0; n < item.quantity(); n++) {
                Ticket ticket = Ticket.builder()
                        .orderDetail(detail)
                        .status(TicketStatus.VALID)
                        .build();
                detail.getTickets().add(ticket);
            }

            order.getOrderDetails().add(detail);

            // La tarifa está gestionada por la transacción; el cambio se flush-ea al commit.
            ticketType.setAvailableCapacity(ticketType.getAvailableCapacity() - item.quantity());
        }

        double totalAmount = round2(totalSubtotal - totalDiscount);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(round2(totalDiscount));

        Order saved = orderRepository.saveAndFlush(order);
        log.info("[Ticket] Compra creada id={} usuario='{}' total={} entradas={}",
                saved.getId(), userEmail, totalAmount, totalQuantity);

        return toOrderDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDto> findMyTickets(String userEmail) {
        User user = getUserOrThrow(userEmail);
        return ticketRepository.findByUserId(user.getId())
                .stream()
                .map(this::toTicketDto)
                .toList();
    }


    // --------
    // Helpers
    // --------
    private void validateNoDuplicateTicketTypes(List<OrderRequestDto.Item> items) {
        Set<Long> ids = new HashSet<>();

        for (OrderRequestDto.Item item : items) {
            if (!ids.add(item.ticketTypeId())) {
                throw new IllegalArgumentException("No se puede repetir la misma tarifa en una compra");
            }
        }
    }

    private void validateTicketTypeForPurchase(TicketType ticketType, int quantity) {
        if (!ticketType.isActive()) {
            throw new IllegalArgumentException(
                    "La tarifa '" + ticketType.getName() + "' no está disponible para la venta");
        }

        Event event = ticketType.getEvent();
        if (event == null) {
            throw new IllegalArgumentException("La tarifa no está asociada a un evento");
        }

        if (!event.isActive() || event.getStatus() != EventStatus.PUBLISHED) {
            throw new IllegalArgumentException("El evento no está disponible para la venta");
        }

        LocalDateTime eventDateTime = LocalDateTime.of(event.getDate(), event.getStartTime());
        if (!eventDateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("El evento ya no está disponible para la venta");
        }

        LocalDate today = LocalDate.now();
        if (ticketType.getSaleStartDate() != null && today.isBefore(ticketType.getSaleStartDate())) {
            throw new IllegalArgumentException(
                    "La venta de la tarifa '" + ticketType.getName() + "' aún no ha comenzado");
        }

        if (ticketType.getSaleEndDate() != null && today.isAfter(ticketType.getSaleEndDate())) {
            throw new IllegalArgumentException(
                    "La venta de la tarifa '" + ticketType.getName() + "' ha finalizado");
        }

        int maxPerPurchase = ticketType.getMaxPerPurchase() != null
                ? ticketType.getMaxPerPurchase()
                : TicketType.DEFAULT_MAX_PER_PURCHASE;
        if (quantity > maxPerPurchase) {
            throw new IllegalArgumentException(
                    "Solo se pueden comprar hasta " + maxPerPurchase +
                            " entradas de la tarifa '" + ticketType.getName() + "'");
        }

        if (ticketType.getAvailableCapacity() == null || ticketType.getAvailableCapacity() < quantity) {
            throw new IllegalStateException(
                    "No hay stock suficiente para la tarifa '" + ticketType.getName() + "'");
        }
    }

    private boolean isPromoUser(User user) {
        String firstName = normalizeFirstName(user.getFirstName());
        return "roberto".equals(firstName) || "geronimo".equals(firstName);
    }

    private String normalizeFirstName(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase(Locale.ROOT);

        return normalized.split("\\s+")[0];
    }

    private User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario con el email: " + email));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private OrderResponseDto toOrderDto(Order order) {
        List<OrderResponseDto.Detail> details = order.getOrderDetails().stream()
                .map(detail -> new OrderResponseDto.Detail(
                        detail.getTicketType().getId(),
                        detail.getTicketType().getName(),
                        detail.getQuantity(),
                        detail.getUnitPrice(),
                        detail.getSubtotal(),
                        detail.getDiscountApplied(),
                        detail.getTickets().stream()
                                .map(this::toTicketDto)
                                .toList()
                ))
                .toList();

        return new OrderResponseDto(
                order.getId(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getCreatedAt(),
                details
        );
    }

    private TicketResponseDto toTicketDto(Ticket ticket) {
        OrderDetail detail = ticket.getOrderDetail();
        TicketType ticketType = detail.getTicketType();
        Event event = ticketType.getEvent();

        return new TicketResponseDto(
                ticket.getId(),
                detail.getOrder().getId(),
                event.getId(),
                event.getTitle(),
                ticketType.getName(),
                LocalDateTime.of(event.getDate(), event.getStartTime()),
                event.getLocation(),
                ticket.getStatus()
        );
    }


}
