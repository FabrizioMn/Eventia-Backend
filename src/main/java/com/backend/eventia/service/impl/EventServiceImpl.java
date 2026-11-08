package com.backend.eventia.service.impl;

import com.backend.eventia.dto.EventInactivateRequestDto;
import com.backend.eventia.dto.EventRequestDto;
import com.backend.eventia.dto.EventResponseDto;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public EventResponseDto create(EventRequestDto request, String organizerEmail) {
        User organizer = requireUser(organizerEmail);
        requireOrganizerOrAdmin(organizer);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con id " + request.categoryId()));
        if (!category.isActive()) {
            throw new IllegalArgumentException("La categoria seleccionada no esta activa");
        }
        validateTimes(request);

        Event event = Event.builder()
                .title(request.title().trim())
                .description(request.description())
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .location(request.location().trim())
                .city(request.city() != null ? request.city().trim() : null)
                .capacity(request.capacity())
                .availableCapacity(request.capacity())
                .status(request.status() != null ? request.status() : EventStatus.BORRADOR)
                .active(true)
                .category(category)
                .organizer(organizer)
                .build();

        return toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDto> listPublic() {
        return eventRepository.findByActiveTrueAndStatus(EventStatus.PUBLICADO).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDto> listByOrganizer(String organizerEmail) {
        User organizer = requireUser(organizerEmail);
        return eventRepository.findByOrganizerId(organizer.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDto> listAll() {
        return eventRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDto getById(Long id) {
        return toResponse(requireEvent(id));
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDto getPublicById(Long id) {
        Event event = eventRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id " + id));
        if (event.getStatus() != EventStatus.PUBLICADO) {
            throw new ResourceNotFoundException("Evento no encontrado con id " + id);
        }
        return toResponse(event);
    }

    @Override
    public EventResponseDto update(Long id, EventRequestDto request, String actorEmail) {
        User actor = requireUser(actorEmail);
        Event event = requireEvent(id);
        requireOwnerOrAdmin(actor, event);
        validateTimes(request);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con id " + request.categoryId()));
        if (!category.isActive()) {
            throw new IllegalArgumentException("La categoria seleccionada no esta activa");
        }

        event.setTitle(request.title().trim());
        event.setDescription(request.description());
        event.setDate(request.date());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setLocation(request.location().trim());
        event.setCity(request.city() != null ? request.city().trim() : null);
        event.setCapacity(request.capacity());
        if (event.getAvailableCapacity() == null || event.getAvailableCapacity() > request.capacity()) {
            event.setAvailableCapacity(request.capacity());
        }
        event.setCategory(category);
        if (request.status() != null) {
            event.setStatus(request.status());
        }
        if (event.isActive()) {
            event.setInactivationReason(null);
        }

        return toResponse(event);
    }

    @Override
    public EventResponseDto inactivate(Long id, EventInactivateRequestDto request, String actorEmail) {
        User actor = requireUser(actorEmail);
        Event event = requireEvent(id);
        requireOwnerOrAdmin(actor, event);

        event.setActive(false);
        event.setStatus(EventStatus.OCULTO);
        event.setInactivationReason(request.reason());
        return toResponse(event);
    }

    @Override
    public void softDelete(Long id, String actorEmail) {
        User actor = requireUser(actorEmail);
        Event event = requireEvent(id);
        requireOwnerOrAdmin(actor, event);

        event.setActive(false);
        event.setStatus(EventStatus.OCULTO);
    }

    private Event requireEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id " + id));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email " + email));
    }

    private void requireOrganizerOrAdmin(User user) {
        if (user.getRole() != UserRole.ORGANIZER && user.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Solo organizadores o administradores pueden gestionar eventos");
        }
    }

    private void requireOwnerOrAdmin(User actor, Event event) {
        boolean isAdmin = actor.getRole() == UserRole.ADMIN;
        boolean isOwner = event.getOrganizer() != null && event.getOrganizer().getId().equals(actor.getId());
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("No tiene permisos para modificar este evento");
        }
        if (!isAdmin && actor.getRole() != UserRole.ORGANIZER) {
            throw new AccessDeniedException("No tiene permisos para modificar este evento");
        }
    }

    private void validateTimes(EventRequestDto request) {
        if (request.endTime() != null && request.startTime() != null && !request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }
    }

    private EventResponseDto toResponse(Event event) {
        String organizerName = event.getOrganizer() != null
                ? (event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName()).trim()
                : null;
        return new EventResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation(),
                event.getCity(),
                event.getCapacity(),
                event.getAvailableCapacity(),
                event.getStatus(),
                event.isActive(),
                event.getInactivationReason(),
                event.getCategory() != null ? event.getCategory().getId() : null,
                event.getCategory() != null ? event.getCategory().getName() : null,
                event.getOrganizer() != null ? event.getOrganizer().getId() : null,
                organizerName,
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
