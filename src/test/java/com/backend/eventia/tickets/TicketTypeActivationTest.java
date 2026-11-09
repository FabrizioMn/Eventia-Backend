package com.backend.eventia.tickets;

import com.backend.eventia.model.Category;
import com.backend.eventia.model.Event;
import com.backend.eventia.model.TicketType;
import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.EventStatus;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.CategoryRepository;
import com.backend.eventia.repository.EventRepository;
import com.backend.eventia.repository.TicketTypeRepository;
import com.backend.eventia.repository.UserRepository;
import com.backend.eventia.security.JwtUtils;
import com.backend.eventia.support.TestUserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TicketTypeActivationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private String organizerToken;

    @BeforeEach
    void setUp() {
        User organizer = TestUserFactory.createUser(
                userRepository, passwordEncoder, "org-activation@test.com", "secret123", UserRole.ORGANIZER, true);
        organizerToken = jwtUtils.generateToken(organizer);
    }

    @Test
    void reactivarTarifaSinAforoSuficienteDevuelve400() throws Exception {
        Event event = createEvent(100);
        createTicketType("Activa", event, 60, true);
        TicketType inactive = createTicketType("Inactiva", event, 50, false);

        mockMvc.perform(patch("/api/ticket-types/{id}/status", inactive.getId())
                        .param("active", "true")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isBadRequest());

        assertFalse(ticketTypeRepository.findById(inactive.getId()).orElseThrow().isActive());
    }

    @Test
    void reactivarTarifaConAforoDisponibleDevuelve204() throws Exception {
        Event event = createEvent(100);
        createTicketType("Activa", event, 40, true);
        TicketType inactive = createTicketType("Inactiva", event, 50, false);

        mockMvc.perform(patch("/api/ticket-types/{id}/status", inactive.getId())
                        .param("active", "true")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isNoContent());

        assertTrue(ticketTypeRepository.findById(inactive.getId()).orElseThrow().isActive());
    }

    private Event createEvent(int capacity) {
        Category category = categoryRepository.save(
                Category.builder().name("Cat Activación").description("d").active(true).build());
        return eventRepository.save(Event.builder()
                .title("Evento Activación")
                .description("d")
                .date(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .location("Lima")
                .capacity(capacity)
                .status(EventStatus.PUBLISHED)
                .active(true)
                .category(category)
                .organizer(userRepository.findByEmail("org-activation@test.com").orElseThrow())
                .build());
    }

    private TicketType createTicketType(String name, Event event, int capacity, boolean active) {
        return ticketTypeRepository.save(TicketType.builder()
                .name(name)
                .zone("General")
                .price(50.0)
                .totalCapacity(capacity)
                .availableCapacity(capacity)
                .isPresale(false)
                .active(active)
                .event(event)
                .build());
    }
}
