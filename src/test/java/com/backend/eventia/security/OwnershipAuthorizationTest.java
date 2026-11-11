package com.backend.eventia.security;

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
import com.backend.eventia.support.TestUserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OwnershipAuthorizationTest {

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

    private String orgAToken;
    private String adminToken;
    private Event eventB;
    private TicketType ticketB;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        User orgA = TestUserFactory.createUser(userRepository, passwordEncoder,
                "orga@test.com", "secret123", UserRole.ORGANIZER, true, "91000001");
        User orgB = TestUserFactory.createUser(userRepository, passwordEncoder,
                "orgb@test.com", "secret123", UserRole.ORGANIZER, true, "91000002");
        User admin = TestUserFactory.createUser(userRepository, passwordEncoder,
                "admin-own@test.com", "secret123", UserRole.ADMIN, true, "91000003");
        orgAToken = jwtUtils.generateToken(orgA);
        adminToken = jwtUtils.generateToken(admin);

        Category category = categoryRepository.save(Category.builder().name("Own Cat").description("d").active(true).build());
        categoryId = category.getId();
        eventB = eventRepository.save(Event.builder()
                .title("Evento B")
                .description("d")
                .date(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .location("Lima")
                .capacity(100)
                .status(EventStatus.PENDING)
                .active(true)
                .category(category)
                .organizer(orgB)
                .build());
        ticketB = ticketTypeRepository.save(TicketType.builder()
                .name("General B")
                .zone("General")
                .price(10.0)
                .totalCapacity(50)
                .availableCapacity(50)
                .maxPerPurchase(6)
                .isPresale(false)
                .active(true)
                .event(eventB)
                .build());
    }

    @Test
    void organizerANoPuedeEditarEventoDeB() throws Exception {
        mockMvc.perform(put("/api/events/{id}", eventB.getId())
                        .header("Authorization", "Bearer " + orgAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Editado", eventB.getDate().plusDays(1).toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizerANoPuedeGestionarTarifasDeB() throws Exception {
        mockMvc.perform(post("/api/events/{eventId}/ticket-types", eventB.getId())
                        .header("Authorization", "Bearer " + orgAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ticketJson("Nueva", 20)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/ticket-types/{id}", ticketB.getId())
                        .header("Authorization", "Bearer " + orgAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ticketJson("Editada", 30)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeEditarEventoDeCualquierOrganizador() throws Exception {
        mockMvc.perform(put("/api/events/{id}", eventB.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Admin Edit", eventB.getDate().plusDays(2).toString())))
                .andExpect(status().isOk());
    }

    private String eventJson(String title, String date) {
        return "{\"title\":\"" + title + "\",\"description\":\"d\",\"date\":\"" + date + "\","
                + "\"startTime\":\"18:00:00\",\"endTime\":\"20:00:00\",\"location\":\"Lima\","
                + "\"capacity\":100,\"categoryId\":" + categoryId + ",\"status\":\"DRAFT\"}";
    }

    private String ticketJson(String name, int capacity) {
        return "{\"name\":\"" + name + "\",\"zone\":\"General\",\"price\":10.0,"
                + "\"totalCapacity\":" + capacity + "}";
    }
}
