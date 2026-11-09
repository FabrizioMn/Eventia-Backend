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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TicketTypeLimitTest {

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

    private Event event;
    private String organizerToken;

    @BeforeEach
    void setUp() {
        User organizer = TestUserFactory.createUser(
                userRepository, passwordEncoder, "org-limit@test.com", "secret123", UserRole.ORGANIZER, true);
        organizerToken = jwtUtils.generateToken(organizer);

        Category category = categoryRepository.save(
                Category.builder().name("Cat Límite").description("d").active(true).build());
        event = eventRepository.save(Event.builder()
                .title("Evento Límite")
                .description("d")
                .date(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .location("Lima")
                .capacity(100)
                .status(EventStatus.PUBLISHED)
                .active(true)
                .category(category)
                .organizer(organizer)
                .build());
    }

    @Test
    void crearTarifaSinLimiteUsaDiezPorDefecto() throws Exception {
        mockMvc.perform(post("/api/events/{eventId}/ticket-types", event.getId())
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"General\",\"zone\":\"General\",\"price\":50.0,\"totalCapacity\":60}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maxPerPurchase").value(10));
    }

    @Test
    void actualizarTarifaLegacySinLimiteUsaDiezPorDefecto() throws Exception {
        TicketType legacy = ticketTypeRepository.save(TicketType.builder()
                .name("Legacy")
                .zone("General")
                .price(20.0)
                .totalCapacity(10)
                .availableCapacity(10)
                .active(true)
                .event(event)
                .build());
        legacy.setMaxPerPurchase(null);
        ticketTypeRepository.saveAndFlush(legacy);

        mockMvc.perform(put("/api/ticket-types/{id}", legacy.getId())
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Legacy\",\"zone\":\"General\",\"price\":20.0,\"totalCapacity\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxPerPurchase").value(10));
    }
}
