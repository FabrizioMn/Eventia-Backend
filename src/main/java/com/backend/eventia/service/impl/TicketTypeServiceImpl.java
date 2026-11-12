package com.backend.eventia.service.impl;

import com.backend.eventia.dto.tickettype.TicketTypeRequestDto;
import com.backend.eventia.dto.tickettype.TicketTypeResponseDto;
import com.backend.eventia.exception.DuplicateResourceException;
import com.backend.eventia.exception.ResourceNotFoundException;
import com.backend.eventia.model.Event;
import com.backend.eventia.model.TicketType;
import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.EventStatus;
import com.backend.eventia.repository.EventRepository;
import com.backend.eventia.repository.TicketTypeRepository;
import com.backend.eventia.repository.UserRepository;
import com.backend.eventia.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TicketTypeResponseDto> findByEvent(Long eventId, boolean includeInactive) {
        log.debug("[TicketType] findByEvent(eventId={}, includeInactive={})", eventId, includeInactive);

        Event event = getEventOrThrow(eventId);
        boolean privileged = isAuthorizedToViewInactive(event);

        if (!privileged && (!event.isActive() || event.getStatus() != EventStatus.PUBLISHED)) {
            log.warn("[TicketType] Intento no autorizado de consultar tarifas de evento no publicado o inactivo id={}", eventId);
            throw new ResourceNotFoundException("No se encontró el evento con el id: " + eventId);
        }

        if (includeInactive && !privileged) {
            log.warn("[TicketType] Acceso denegado: usuario sin permisos intentó ver tarifas inactivas del evento id={}", eventId);
            throw new AccessDeniedException("No tienes permisos para ver tarifas inactivas de este evento");
        }

        List<TicketType> list = includeInactive
                ? ticketTypeRepository.findByEventId(eventId)
                : ticketTypeRepository.findByEventIdAndActiveTrue(eventId);

        log.debug("[TicketType] findByEvent -> {} tarifas encontradas", list.size());
        return list.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public TicketTypeResponseDto create(Long eventId, TicketTypeRequestDto request, String userEmail) {
        String name = request.name().trim();
        log.info("[TicketType] Creando tarifa '{}' para evento id={} por usuario='{}'", name, eventId, userEmail);

        Event event = getEventOrThrow(eventId);
        User user = getUserOrThrow(userEmail);
        validateEventOwnership(event, user);

        ensureNameIsAvailable(name, eventId, null);
        validateSaleDates(request.saleStartDate(), request.saleEndDate(), event.getDate());
        validateCapacityForCreation(event, request.totalCapacity());

        TicketType ticketType = TicketType.builder()
                .name(name)
                .zone(request.zone() != null ? request.zone().trim() : "General")
                .price(request.price())
                .totalCapacity(request.totalCapacity())
                .availableCapacity(request.totalCapacity())
                .maxPerPurchase(request.maxPerPurchase() != null
                        ? request.maxPerPurchase()
                        : TicketType.DEFAULT_MAX_PER_PURCHASE)
                .isPresale(request.isPresale() != null && request.isPresale())
                .saleStartDate(request.saleStartDate())
                .saleEndDate(request.saleEndDate())
                .active(true)
                .event(event)
                .build();

        ticketTypeRepository.save(ticketType);
        log.info("[TicketType] Tarifa creada exitosamente id={} nombre='{}' aforo={}",
                ticketType.getId(), ticketType.getName(), ticketType.getTotalCapacity());

        return toDto(ticketType);
    }

    @Override
    @Transactional
    public TicketTypeResponseDto update(Long id, TicketTypeRequestDto request, String userEmail) {
        log.info("[TicketType] Actualizando tarifa id={} por usuario='{}'", id, userEmail);

        TicketType ticketType = getTicketTypeOrThrow(id);
        Event event = ticketType.getEvent();
        User user = getUserOrThrow(userEmail);
        validateEventOwnership(event, user);

        String newName = request.name().trim();
        ensureNameIsAvailable(newName, event.getId(), ticketType);
        validateSaleDates(request.saleStartDate(), request.saleEndDate(), event.getDate());

        validateCapacityForUpdate(event, id, request.totalCapacity(), ticketType);

        ticketType.setName(newName);
        ticketType.setPrice(request.price());

        if (request.zone() != null) {
            ticketType.setZone(request.zone().trim());
        }
        if (request.maxPerPurchase() != null) {
            ticketType.setMaxPerPurchase(request.maxPerPurchase());
        } else if (ticketType.getMaxPerPurchase() == null) {
            ticketType.setMaxPerPurchase(TicketType.DEFAULT_MAX_PER_PURCHASE);
        }
        if (request.isPresale() != null) {
            ticketType.setPresale(request.isPresale());
        }
        ticketType.setSaleStartDate(request.saleStartDate());
        ticketType.setSaleEndDate(request.saleEndDate());

        ticketTypeRepository.save(ticketType);
        log.info("[TicketType] Tarifa actualizada exitosamente id={} nombre='{}'", ticketType.getId(), ticketType.getName());

        return toDto(ticketType);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, boolean active, String userEmail) {
        log.info("[TicketType] Cambiando estado de tarifa id={} a active={} por usuario='{}'", id, active, userEmail);

        TicketType ticketType = getTicketTypeOrThrow(id);
        User user = getUserOrThrow(userEmail);
        validateEventOwnership(ticketType.getEvent(), user);

        if (active && !ticketType.isActive()) {
            validateCapacityForReactivation(ticketType);
        }

        ticketType.setActive(active);
        ticketTypeRepository.save(ticketType);

        log.info("[TicketType] Estado de tarifa id={} actualizado a active={}", ticketType.getId(), ticketType.isActive());
    }

    @Override
    @Transactional
    public void delete(Long id, String userEmail) {
        log.info("[TicketType] Inactivando tarifa (baja lógica) id={} por usuario='{}'", id, userEmail);

        TicketType ticketType = getTicketTypeOrThrow(id);
        User user = getUserOrThrow(userEmail);
        validateEventOwnership(ticketType.getEvent(), user);

        ticketType.setActive(false);
        ticketTypeRepository.save(ticketType);
        log.info("[TicketType] Tarifa id={} inactivada exitosamente", id);
    }

    // --------
    // Helpers
    // --------

    private TicketType getTicketTypeOrThrow(Long id) {
        return ticketTypeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[TicketType] No se encontró la tarifa id={}", id);
                    return new ResourceNotFoundException("No se encontró la tarifa con el id: " + id);
                });
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("[TicketType] Evento id={} no encontrado", eventId);
                    return new ResourceNotFoundException("No se encontró el evento con el id: " + eventId);
                });
    }

    private User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[TicketType] Usuario email='{}' no encontrado", email);
                    return new ResourceNotFoundException("No se encontró el usuario con el email: " + email);
                });
    }

    private void validateEventOwnership(Event event, User user) {
        boolean isOwner = event.getOrganizer().getId().equals(user.getId());

        if (!isOwner) {
            log.warn("[TicketType] Acceso denegado: usuario '{}' no es dueño del evento id={}", user.getEmail(), event.getId());
            throw new AccessDeniedException("No tienes permisos para gestionar tarifas de este evento");
        }
    }

    private boolean isAuthorizedToViewInactive(Event event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return false;
        }

        return userRepository.findByEmail(auth.getName())
                .map(u -> event.getOrganizer().getId().equals(u.getId()))
                .orElse(false);
    }

    private void validateSaleDates(LocalDate saleStartDate, LocalDate saleEndDate, LocalDate eventDate) {
        if (saleStartDate != null && saleEndDate != null && saleEndDate.isBefore(saleStartDate)) {
            log.warn("[TicketType] Fechas de venta inválidas: inicio={} fin={}", saleStartDate, saleEndDate);
            throw new IllegalArgumentException("La fecha de fin de venta no puede ser anterior a la fecha de inicio de venta");
        }
        if (eventDate != null && saleEndDate != null && saleEndDate.isAfter(eventDate)) {
            log.warn("[TicketType] Fecha fin de venta posterior al evento: fin={} evento={}", saleEndDate, eventDate);
            throw new IllegalArgumentException("La fecha de fin de venta no puede ser posterior a la fecha del evento");
        }
        if (eventDate != null && saleStartDate != null && saleStartDate.isAfter(eventDate)) {
            log.warn("[TicketType] Fecha inicio de venta posterior al evento: inicio={} evento={}", saleStartDate, eventDate);
            throw new IllegalArgumentException("La fecha de inicio de venta no puede ser posterior a la fecha del evento");
        }
    }

    private void validateCapacityForCreation(Event event, int newCapacity) {
        int currentTotal = ticketTypeRepository.sumCapacityByEventId(event.getId());
        if (currentTotal + newCapacity > event.getCapacity()) {
            int available = Math.max(0, event.getCapacity() - currentTotal);
            log.warn("[TicketType] Capacidad excedida: aforo evento={}, suma actual={}, requerida={}, disponible={}",
                    event.getCapacity(), currentTotal, newCapacity, available);
            throw new IllegalArgumentException(
                    "El aforo de las tarifas (" + (currentTotal + newCapacity) +
                    ") supera la capacidad máxima del evento (" + event.getCapacity() +
                    "). Aforo disponible restante: " + available
            );
        }
    }

    private void validateCapacityForUpdate(Event event, Long ticketTypeId, int newCapacity, TicketType currentTicket) {
        int otherTicketsCapacity = ticketTypeRepository.sumCapacityByEventIdExcluding(event.getId(), ticketTypeId);
        if (otherTicketsCapacity + newCapacity > event.getCapacity()) {
            int available = Math.max(0, event.getCapacity() - otherTicketsCapacity);
            log.warn("[TicketType] Actualización de aforo excede límite: evento={}, otras={}, nueva={}",
                    event.getCapacity(), otherTicketsCapacity, newCapacity);
            throw new IllegalArgumentException(
                    "La capacidad total de las tarifas (" + (otherTicketsCapacity + newCapacity) +
                    ") supera la capacidad máxima del evento (" + event.getCapacity() +
                    "). Aforo disponible restante: " + available
            );
        }

        int soldCount = currentTicket.getTotalCapacity() - currentTicket.getAvailableCapacity();
        if (newCapacity < soldCount) {
            throw new IllegalArgumentException(
                    "No puedes reducir la capacidad a " + newCapacity +
                    " porque ya se han vendido " + soldCount + " entradas."
            );
        }

        // Ajustar availableCapacity manteniendo la diferencia de vendidas
        currentTicket.setTotalCapacity(newCapacity);
        currentTicket.setAvailableCapacity(newCapacity - soldCount);
    }

    private void validateCapacityForReactivation(TicketType ticketType) {
        Event event = ticketType.getEvent();
        int otherTicketsCapacity = ticketTypeRepository.sumCapacityByEventIdExcluding(
                event.getId(), ticketType.getId());
        int totalCapacity = otherTicketsCapacity + ticketType.getTotalCapacity();

        if (totalCapacity > event.getCapacity()) {
            int available = Math.max(0, event.getCapacity() - otherTicketsCapacity);
            log.warn("[TicketType] Reactivación rechazada por aforo: evento={}, otras={}, tarifa={}, disponible={}",
                    event.getCapacity(), otherTicketsCapacity, ticketType.getTotalCapacity(), available);
            throw new IllegalArgumentException(
                    "No se puede reactivar la tarifa: el aforo total de las tarifas (" + totalCapacity +
                    ") supera la capacidad máxima del evento (" + event.getCapacity() +
                    "). Aforo disponible restante: " + available
            );
        }
    }

    private void ensureNameIsAvailable(String name, Long eventId, TicketType currentTicket) {
        if (currentTicket != null && currentTicket.getName().equalsIgnoreCase(name)) {
            return;
        }

        if (ticketTypeRepository.existsByNameIgnoreCaseAndEventId(name, eventId)) {
            log.warn("[TicketType] Ya existe una tarifa con el nombre '{}' para el evento id={}", name, eventId);
            throw new DuplicateResourceException("Ya existe una tarifa con el nombre '" + name + "' en este evento");
        }
    }

    private String computeStatus(TicketType t) {
        if (!t.isActive()) {
            return "paused";
        }
        if (t.getAvailableCapacity() != null && t.getAvailableCapacity() <= 0) {
            return "sold_out";
        }
        return "active";
    }

    private TicketTypeResponseDto toDto(TicketType t) {
        int soldCount = (t.getTotalCapacity() != null && t.getAvailableCapacity() != null)
                ? (t.getTotalCapacity() - t.getAvailableCapacity())
                : 0;

        return new TicketTypeResponseDto(
                t.getId(),
                t.getEvent() != null ? t.getEvent().getId() : null,
                t.getEvent() != null ? t.getEvent().getTitle() : null,
                t.getName(),
                t.getZone(),
                t.getPrice(),
                t.getTotalCapacity(),
                t.getAvailableCapacity(),
                soldCount,
                computeStatus(t),
                t.getMaxPerPurchase(),
                t.isPresale(),
                t.getSaleStartDate(),
                t.getSaleEndDate(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}

