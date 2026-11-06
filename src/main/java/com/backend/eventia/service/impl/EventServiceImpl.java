package com.backend.eventia.service.impl;

import com.backend.eventia.dto.event.EventDetailResponseDto;
import com.backend.eventia.dto.event.EventRequestDto;
import com.backend.eventia.dto.event.EventSummaryResponseDto;
import com.backend.eventia.exception.DuplicateResourceException;
import com.backend.eventia.exception.ResourceNotFoundException;
import com.backend.eventia.model.Category;
import com.backend.eventia.model.Event;
import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.EventStatus;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.CategoryRepository;
import com.backend.eventia.repository.EventRepository;
import com.backend.eventia.repository.UserRepository;
import com.backend.eventia.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryResponseDto> findAll(boolean includeInactive, Pageable pageable) {
        log.debug("[Event] findAll(includeInactive={}, page={}, size={})",
                includeInactive, pageable.getPageNumber(), pageable.getPageSize());

        Page<Event> events = includeInactive
                ? eventRepository.findAll(pageable)
                : eventRepository.findByActiveTrue(pageable);

        log.debug("[Event] findAll -> {} eventos encontrados en página {}", events.getNumberOfElements(), pageable.getPageNumber());
        return events.map(this::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryResponseDto> findByOrganizer(String organizerEmail, boolean includeInactive, Pageable pageable) {
        log.debug("[Event] findByOrganizer(organizerEmail='{}', includeInactive={}, page={}, size={})",
                organizerEmail, includeInactive, pageable.getPageNumber(), pageable.getPageSize());

        User organizer = getUserOrThrow(organizerEmail);

        Page<Event> events = includeInactive
                ? eventRepository.findByOrganizerId(organizer.getId(), pageable)
                : eventRepository.findByOrganizerIdAndActiveTrue(organizer.getId(), pageable);

        log.debug("[Event] findByOrganizer -> {} eventos propios encontrados", events.getNumberOfElements());
        return events.map(this::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public EventDetailResponseDto findById(Long id) {
        log.debug("[Event] Buscando evento id={}", id);
        return toDetailDto(getEventOrThrow(id));
    }

    @Override
    @Transactional
    public EventDetailResponseDto create(EventRequestDto request, String organizerEmail) {
        String title = request.title().trim();
        log.info("[Event] Creando evento '{}' para organizador='{}'", title, organizerEmail);

        User organizer = getUserOrThrow(organizerEmail);
        Category category = getCategoryOrThrow(request.categoryId());

        validateSchedule(request.date(), request.startTime(), request.endTime());
        ensureTitleIsAvailable(title, organizer.getId(), null);

        EventStatus initialStatus = request.status() != null ? request.status() : EventStatus.PENDING;

        Event event = Event.builder()
                .title(title)
                .description(request.description())
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .location(request.location())
                .capacity(request.capacity())
                .bannerUrl(request.bannerUrl())
                .status(initialStatus)
                .active(true)
                .category(category)
                .organizer(organizer)
                .build();

        eventRepository.save(event);
        log.info("[Event] Evento creado exitosamente id={} título='{}'", event.getId(), event.getTitle());

        return toDetailDto(event);
    }

    @Override
    @Transactional
    public EventDetailResponseDto update(Long id, EventRequestDto request, String userEmail) {
        log.info("[Event] Actualizando evento id={} por usuario='{}'", id, userEmail);

        Event event = getEventOrThrow(id);
        User user = getUserOrThrow(userEmail);
        validateOwnership(event, user);

        String newTitle = request.title().trim();
        ensureTitleIsAvailable(newTitle, event.getOrganizer().getId(), event);

        Category category = getCategoryOrThrow(request.categoryId());
        validateSchedule(request.date(), request.startTime(), request.endTime());

        event.setTitle(newTitle);
        event.setDescription(request.description());
        event.setDate(request.date());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setLocation(request.location());
        event.setCapacity(request.capacity());
        event.setCategory(category);

        if (request.bannerUrl() != null) {
            event.setBannerUrl(request.bannerUrl());
        }
        if (request.status() != null) {
            event.setStatus(request.status());
        }

        eventRepository.save(event);
        log.info("[Event] Evento actualizado id={} título='{}'", event.getId(), event.getTitle());

        return toDetailDto(event);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, EventStatus status, String userEmail) {
        log.info("[Event] Cambiando estado de evento id={} a status={} por usuario='{}'", id, status, userEmail);

        Event event = getEventOrThrow(id);
        User user = getUserOrThrow(userEmail);
        validateOwnership(event, user);

        event.setStatus(status);
        eventRepository.save(event);

        log.info("[Event] Estado de evento actualizado id={} status={}", event.getId(), event.getStatus());
    }

    @Override
    @Transactional
    public void updateActive(Long id, boolean active, String userEmail) {
        log.info("[Event] Cambiando estado de activación id={} active={} por usuario='{}'", id, active, userEmail);

        Event event = getEventOrThrow(id);
        User user = getUserOrThrow(userEmail);
        validateOwnership(event, user);

        event.setActive(active);
        if (!active && event.getStatus() != EventStatus.FINISHED) {
            event.setStatus(EventStatus.HIDDEN);
        }

        eventRepository.save(event);
        log.info("[Event] Visibilidad activa de evento actualizada id={} active={}", event.getId(), event.isActive());
    }

    // --------
    // Helpers
    // --------

    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[Event] No se encontró el evento con id={}", id);
                    return new ResourceNotFoundException("No se encontró el evento con el id: " + id);
                });
    }

    private Category getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("[Event] Categoría id={} no encontrada", categoryId);
                    return new ResourceNotFoundException("No se encontró la categoría con el id: " + categoryId);
                });
    }

    private User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[Event] Usuario email='{}' no encontrado", email);
                    return new ResourceNotFoundException("No se encontró el usuario con el email: " + email);
                });
    }

    private void validateOwnership(Event event, User user) {
        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        boolean isOwner = event.getOrganizer().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            log.warn("[Event] Acceso denegado: usuario id={} no es dueño del evento id={}", user.getId(), event.getId());
            throw new AccessDeniedException("No tienes permisos para modificar este evento");
        }
    }

    private void validateSchedule(LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            log.warn("[Event] Horario inválido: startTime={}, endTime={}", startTime, endTime);
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }
    }

    private void ensureTitleIsAvailable(String title, Long organizerId, Event currentEvent) {
        if (currentEvent != null && currentEvent.getTitle().equalsIgnoreCase(title)) {
            return;
        }

        if (eventRepository.existsByTitleIgnoreCaseAndOrganizerId(title, organizerId)) {
            log.warn("[Event] Título duplicado para organizador id={}: '{}'", organizerId, title);
            throw new DuplicateResourceException("Ya tienes un evento registrado con el título: " + title);
        }
    }

    private EventSummaryResponseDto toSummaryDto(Event event) {
        return new EventSummaryResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation(),
                event.getCapacity(),
                event.getBannerUrl(),
                event.getCategory() != null ? event.getCategory().getName() : null,
                event.getStatus(),
                event.isActive()
        );
    }

    private EventDetailResponseDto toDetailDto(Event event) {
        return new EventDetailResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation(),
                event.getCapacity(),
                event.getBannerUrl(),
                event.getStatus(),
                event.isActive(),
                event.getCategory() != null ? event.getCategory().getId() : null,
                event.getCategory() != null ? event.getCategory().getName() : null,
                event.getOrganizer() != null ? event.getOrganizer().getId() : null,
                event.getOrganizer() != null ? (event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName()) : null,
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}

