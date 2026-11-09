package com.backend.eventia.service.impl;

import com.backend.eventia.dto.event.EventSummaryResponseDto;
import com.backend.eventia.dto.organizer.*;
import com.backend.eventia.exception.ResourceNotFoundException;
import com.backend.eventia.model.Event;
import com.backend.eventia.model.OrderDetail;
import com.backend.eventia.model.TicketType;
import com.backend.eventia.model.User;
import com.backend.eventia.repository.EventRepository;
import com.backend.eventia.repository.OrderDetailRepository;
import com.backend.eventia.repository.TicketTypeRepository;
import com.backend.eventia.repository.UserRepository;
import com.backend.eventia.service.EventService;
import com.backend.eventia.service.OrganizerDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizerDashboardServiceImpl implements OrganizerDashboardService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final EventService eventService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String[] ZONE_COLORS = {
            "#3525cd", "#571ac0", "#4f46e5", "#7c3aed", "#6366f1", "#4338ca", "#3730a3"
    };

    @Override
    @Transactional(readOnly = true)
    public OrganizerDashboardResponseDto getDashboardData(String organizerEmail) {
        log.info("[OrganizerDashboard] Obteniendo métricas consolidadas para organizador='{}'", organizerEmail);

        User organizer = getUserOrThrow(organizerEmail);
        Long organizerId = organizer.getId();

        List<Event> activeEvents = eventRepository.findByOrganizerIdAndActiveTrue(organizerId);
        List<TicketType> activeTickets = ticketTypeRepository.findByOrganizerIdAndActiveTrue(organizerId);
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrganizerId(organizerId);

        OrganizerKpiDto kpis = buildKpis(activeEvents, activeTickets, organizerId);
        List<DailySalesDataPointDto> dailySales = buildDailySales(orderDetails);
        List<ZoneDistributionDataPointDto> zoneDistribution = buildZoneDistribution(activeTickets);
        List<EventSummaryResponseDto> recentEvents = buildRecentEvents(organizerEmail);
        ActiveShiftDto activeShift = buildActiveShift(activeEvents, kpis.occupancyRate());

        log.debug("[OrganizerDashboard] Métricas calculadas: {} eventos activos, {} entradas vendidas, S/. {} recaudación",
                kpis.activeEvents(), kpis.ticketsSold(), kpis.totalRevenuePEN());

        return new OrganizerDashboardResponseDto(kpis, dailySales, zoneDistribution, recentEvents, activeShift);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizerLiveSaleNotificationDto broadcastSaleNotification(
            String organizerEmail,
            String eventTitle,
            String ticketTypeName,
            Integer quantity,
            Double unitPrice,
            String buyerName) {
        log.info("[OrganizerDashboard] Emitiendo notificación en vivo para organizador='{}': {}x '{}' a S/. {}",
                organizerEmail, quantity, ticketTypeName, unitPrice);

        User organizer = getUserOrThrow(organizerEmail);
        OrganizerDashboardResponseDto freshData = getDashboardData(organizerEmail);

        int safeQty = quantity != null && quantity > 0 ? quantity : 1;
        double safePrice = unitPrice != null && unitPrice >= 0 ? unitPrice : 50.0;
        double subtotal = safeQty * safePrice;
        String safeBuyer = buyerName != null && !buyerName.isBlank() ? buyerName : "Comprador Anónimo";
        String message = String.format("¡Nueva venta! %dx %s (%s) - S/ %.2f",
                safeQty, ticketTypeName, eventTitle, subtotal);

        OrganizerLiveSaleNotificationDto notification = new OrganizerLiveSaleNotificationDto(
                organizer.getId(),
                eventTitle,
                ticketTypeName,
                safeQty,
                safePrice,
                subtotal,
                safeBuyer,
                message,
                LocalDateTime.now(),
                freshData);

        String destination = "/topic/organizers/" + organizer.getId() + "/dashboard";
        log.info("[OrganizerDashboard] Publicando STOMP en destino '{}'", destination);
        messagingTemplate.convertAndSend(destination, notification);

        return notification;
    }

    // --------
    // Helpers
    // --------

    private User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[OrganizerDashboard] Usuario organizador '{}' no encontrado", email);
                    return new ResourceNotFoundException("No se encontró el organizador con email: " + email);
                });
    }

    private OrganizerKpiDto buildKpis(List<Event> activeEvents, List<TicketType> activeTickets, Long organizerId) {
        long activeEventsCount = activeEvents.size();

        Double dbRevenue = orderDetailRepository.sumRevenueByOrganizerId(organizerId);
        double totalRevenue = dbRevenue != null ? dbRevenue : 0.0;

        Integer dbSold = orderDetailRepository.sumTicketsSoldByOrganizerId(organizerId);
        long ticketsSold = dbSold != null && dbSold > 0 ? dbSold.longValue() : 0L;

        // Si aún no hay órdenes registradas pero los tickets tienen ventas reflejadas en su aforo
        if (ticketsSold == 0 && !activeTickets.isEmpty()) {
            for (TicketType t : activeTickets) {
                if (t.getTotalCapacity() != null && t.getAvailableCapacity() != null) {
                    long sold = t.getTotalCapacity() - t.getAvailableCapacity();
                    if (sold > 0) {
                        ticketsSold += sold;
                        totalRevenue += (sold * t.getPrice());
                    }
                }
            }
        }

        long totalCapacity = activeEvents.stream()
                .mapToLong(e -> e.getCapacity() != null ? e.getCapacity() : 0)
                .sum();

        int occupancyRate = totalCapacity > 0
                ? (int) Math.round(((double) ticketsSold / totalCapacity) * 100)
                : 0;

        return new OrganizerKpiDto(
                activeEventsCount,
                null,
                ticketsSold,
                null,
                totalRevenue,
                null,
                occupancyRate,
                null
        );
    }

    private List<DailySalesDataPointDto> buildDailySales(List<OrderDetail> orderDetails) {
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.of("es", "PE"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, DailySalesDataPointDto> salesMap = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        // Inicializar últimos 7 días con 0
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String dateStr = d.format(dateFormatter);
            String dayStr = capitalize(d.format(dayFormatter));
            salesMap.put(dateStr, new DailySalesDataPointDto(dayStr, dateStr, 0.0, 0));
        }

        // Agregar ventas reales si existen
        for (OrderDetail od : orderDetails) {
            if (od.getOrder() != null && od.getOrder().getCreatedAt() != null) {
                LocalDate orderDate = od.getOrder().getCreatedAt().toLocalDate();
                String dateStr = orderDate.format(dateFormatter);
                if (salesMap.containsKey(dateStr)) {
                    DailySalesDataPointDto curr = salesMap.get(dateStr);
                    double subtotal = od.getSubtotal() != null ? od.getSubtotal() : 0.0;
                    int qty = od.getQuantity() != null ? od.getQuantity() : 0;
                    salesMap.put(dateStr, new DailySalesDataPointDto(
                            curr.day(),
                            curr.date(),
                            curr.totalPEN() + subtotal,
                            curr.ticketsSold() + qty
                    ));
                }
            }
        }

        return new ArrayList<>(salesMap.values());
    }

    private List<ZoneDistributionDataPointDto> buildZoneDistribution(List<TicketType> activeTickets) {
        List<ZoneDistributionDataPointDto> list = new ArrayList<>();
        Map<String, long[]> zoneAggregates = new LinkedHashMap<>();

        for (TicketType t : activeTickets) {
            String zoneName = t.getZone() != null && !t.getZone().isBlank() ? t.getZone() : t.getName();
            long capacity = t.getTotalCapacity() != null ? t.getTotalCapacity() : 0;
            long sold = (t.getTotalCapacity() != null && t.getAvailableCapacity() != null)
                    ? Math.max(0, t.getTotalCapacity() - t.getAvailableCapacity())
                    : 0;

            long[] agg = zoneAggregates.computeIfAbsent(zoneName, k -> new long[]{0L, 0L});
            agg[0] += sold;
            agg[1] += capacity;
        }

        int colorIdx = 0;
        for (Map.Entry<String, long[]> entry : zoneAggregates.entrySet()) {
            long sold = entry.getValue()[0];
            long cap = entry.getValue()[1];
            int pct = cap > 0 ? (int) Math.round(((double) sold / cap) * 100) : 0;
            String color = ZONE_COLORS[colorIdx % ZONE_COLORS.length];
            colorIdx++;

            list.add(new ZoneDistributionDataPointDto(entry.getKey(), sold, cap, pct, color));
        }

        return list;
    }

    private List<EventSummaryResponseDto> buildRecentEvents(String organizerEmail) {
        Page<EventSummaryResponseDto> page = eventService.findByOrganizer(
                organizerEmail,
                false,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "date"))
        );
        return page.getContent();
    }

    private ActiveShiftDto buildActiveShift(List<Event> activeEvents, int occupancyRate) {
        String venue = activeEvents.isEmpty() ? "Sin eventos activos hoy" : activeEvents.get(0).getLocation();
        String warning = occupancyRate >= 80 ? "Aforo general al " + occupancyRate + "%" : "Aforo en nivel normal";
        return new ActiveShiftDto(venue, "online", warning);
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
