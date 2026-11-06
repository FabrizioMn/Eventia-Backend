package com.backend.eventia.service.impl;

import com.backend.eventia.dto.admin.ActiveEventResponseDto;
import com.backend.eventia.dto.admin.DashboardKpiResponseDto;
import com.backend.eventia.dto.admin.KpiMetricDto;
import com.backend.eventia.dto.admin.MonitoringDashboardResponseDto;
import com.backend.eventia.dto.admin.RevenueTimelineResponseDto;
import com.backend.eventia.dto.admin.TicketFlowResponseDto;
import com.backend.eventia.model.Category;
import com.backend.eventia.model.Event;
import com.backend.eventia.model.Order;
import com.backend.eventia.model.Ticket;
import com.backend.eventia.model.TicketType;
import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.EventRepository;
import com.backend.eventia.repository.OrderRepository;
import com.backend.eventia.repository.TicketRepository;
import com.backend.eventia.repository.UserRepository;
import com.backend.eventia.service.AdminMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminMonitoringServiceImpl implements AdminMonitoringService {

    private static final Locale LOCALE_ES_PE = Locale.forLanguageTag("es-PE");
    private static final int TIMELINE_MONTHS = 6;

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public MonitoringDashboardResponseDto getDashboardData() {
        return new MonitoringDashboardResponseDto(
                getKpis(),
                getRevenueTimeline(),
                getTicketFlow(),
                getActiveEvents()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardKpiResponseDto getKpis() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        LocalDateTime currentStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime nextStart = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime previousStart = previousMonth.atDay(1).atStartOfDay();

        // 1. Ingresos Totales (Fuente: Orders.total_amount)
        double totalRevenue = orderRepository.sumTotalRevenue();
        double currentMonthRevenue = orderRepository.sumTotalRevenueBetween(currentStart, nextStart);
        double previousMonthRevenue = orderRepository.sumTotalRevenueBetween(previousStart, currentStart);
        KpiMetricDto revenueKpi = buildKpiMetric(totalRevenue, currentMonthRevenue, previousMonthRevenue);

        // 2. Órdenes Procesadas (Fuente: Orders.id)
        long totalOrders = orderRepository.count();
        long currentMonthOrders = orderRepository.countByCreatedAtBetween(currentStart, nextStart);
        long previousMonthOrders = orderRepository.countByCreatedAtBetween(previousStart, currentStart);
        KpiMetricDto ordersKpi = buildKpiMetric(totalOrders, currentMonthOrders, previousMonthOrders);

        // 3. Tickets Emitidos (Fuente: Tickets.id)
        long totalTickets = ticketRepository.count();
        long currentMonthTickets = ticketRepository.countByCreatedAtBetween(currentStart, nextStart);
        long previousMonthTickets = ticketRepository.countByCreatedAtBetween(previousStart, currentStart);
        KpiMetricDto ticketsKpi = buildKpiMetric(totalTickets, currentMonthTickets, previousMonthTickets);

        // 4. Organizadores Activos (Fuente: Users.role = ORGANIZER and active = true)
        long totalActiveOrganizers = userRepository.countByRoleAndActiveTrue(UserRole.ORGANIZER);
        long currentMonthOrganizers = userRepository.countByRoleAndActiveTrueAndCreatedAtBetween(
                UserRole.ORGANIZER, currentStart, nextStart);
        long previousMonthOrganizers = userRepository.countByRoleAndActiveTrueAndCreatedAtBetween(
                UserRole.ORGANIZER, previousStart, currentStart);
        KpiMetricDto organizersKpi = buildKpiMetric(
                totalActiveOrganizers, currentMonthOrganizers, previousMonthOrganizers);

        return new DashboardKpiResponseDto(revenueKpi, ordersKpi, ticketsKpi, organizersKpi);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenueTimelineResponseDto> getRevenueTimeline() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtAsc();
        List<YearMonth> months = buildRecentMonths();
        List<RevenueTimelineResponseDto> timeline = new ArrayList<>(months.size());

        for (YearMonth month : months) {
            double monthlyRevenue = 0.0;
            long monthlyOrders = 0L;

            for (Order order : orders) {
                LocalDateTime createdAt = order.getCreatedAt();
                if (createdAt != null && YearMonth.from(createdAt).equals(month)) {
                    Double amount = order.getTotalAmount();
                    if (amount != null) {
                        monthlyRevenue += amount;
                    }
                    monthlyOrders++;
                }
            }

            timeline.add(new RevenueTimelineResponseDto(
                    formatPeriodLabel(month),
                    roundTwoDecimals(monthlyRevenue),
                    monthlyOrders
            ));
        }

        return timeline;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketFlowResponseDto> getTicketFlow() {
        List<Ticket> tickets = ticketRepository.findAllByOrderByCreatedAtAsc();
        List<YearMonth> months = buildRecentMonths();
        List<TicketFlowResponseDto> flow = new ArrayList<>(months.size());

        for (YearMonth month : months) {
            long issued = 0L;
            long redeemed = 0L;

            for (Ticket ticket : tickets) {
                LocalDateTime createdAt = ticket.getCreatedAt();
                if (createdAt != null && YearMonth.from(createdAt).equals(month)) {
                    issued++;
                }
                LocalDateTime redeemedAt = ticket.getRedeemedAt();
                if (redeemedAt != null && YearMonth.from(redeemedAt).equals(month)) {
                    redeemed++;
                }
            }

            flow.add(new TicketFlowResponseDto(
                    formatPeriodLabel(month),
                    issued,
                    redeemed
            ));
        }

        return flow;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActiveEventResponseDto> getActiveEvents() {
        List<Event> events = eventRepository.findActiveEventsWithDetails();
        List<ActiveEventResponseDto> result = new ArrayList<>(events.size());

        for (Event event : events) {
            Category category = event.getCategory();
            String categoryName = category != null ? category.getName() : "Sin categoría";

            User organizer = event.getOrganizer();
            String organizerName = organizer != null
                    ? (organizer.getFirstName() + " " + organizer.getLastName()).trim()
                    : "Organizador";

            int soldFromTicketTypes = 0;
            List<TicketType> ticketTypes = event.getTicketTypes();
            if (ticketTypes != null) {
                for (TicketType tt : ticketTypes) {
                    Integer totalCap = tt.getTotalCapacity();
                    Integer availableCap = tt.getAvailableCapacity();
                    if (totalCap != null && availableCap != null) {
                        soldFromTicketTypes += Math.max(0, totalCap - availableCap);
                    }
                }
            }

            Long eventId = event.getId();
            long soldFromTicketsTable = eventId != null
                    ? ticketRepository.countTicketsByEventId(eventId)
                    : 0L;
            int ticketsSold = Math.max(soldFromTicketTypes, (int) soldFromTicketsTable);

            Integer capacity = event.getCapacity();
            int safeCapacity = capacity != null ? capacity : 0;

            result.add(new ActiveEventResponseDto(
                    eventId,
                    event.getTitle(),
                    categoryName,
                    organizerName,
                    ticketsSold,
                    safeCapacity,
                    event.getStatus()
            ));
        }

        return result;
    }

    private KpiMetricDto buildKpiMetric(double totalValue, double currentMonthValue, double previousMonthValue) {
        double percentageChange;
        if (previousMonthValue > 0) {
            percentageChange = ((currentMonthValue - previousMonthValue) / previousMonthValue) * 100.0;
        } else if (currentMonthValue > 0) {
            percentageChange = 100.0;
        } else {
            percentageChange = 0.0;
        }
        double roundedPercentage = Math.round(percentageChange * 10.0) / 10.0;
        return new KpiMetricDto(
                roundTwoDecimals(totalValue),
                roundedPercentage,
                roundTwoDecimals(previousMonthValue)
        );
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private List<YearMonth> buildRecentMonths() {
        YearMonth current = YearMonth.now();
        List<YearMonth> months = new ArrayList<>(TIMELINE_MONTHS);
        for (int i = TIMELINE_MONTHS - 1; i >= 0; i--) {
            months.add(current.minusMonths(i));
        }
        return months;
    }

    private String formatPeriodLabel(YearMonth ym) {
        String monthShort = ym.getMonth().getDisplayName(TextStyle.SHORT, LOCALE_ES_PE).replace(".", "");
        if (monthShort.isEmpty()) {
            return ym.toString();
        }
        return Character.toUpperCase(monthShort.charAt(0)) + monthShort.substring(1);
    }
}
