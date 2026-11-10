package com.backend.eventia.rules;

import com.backend.eventia.model.Category;
import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.EventStatus;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.CategoryRepository;
import com.backend.eventia.repository.EventRepository;
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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EventCreationAndPublicationFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private User organizer;
    private String organizerToken;
    private Category category;

    @BeforeEach
    void setUp() {
        organizer = TestUserFactory.createUser(
                userRepository, passwordEncoder, "org-flow@test.com", "secret123", UserRole.ORGANIZER, true, "82000099"
        );
        organizerToken = jwtUtils.generateToken(organizer);

        category = categoryRepository.save(Category.builder()
                .name("Conciertos Flow")
                .description("Categoría para prueba de flujo")
                .active(true)
                .build());
    }

    @Test
    void createEvent_withoutStatus_shouldDefaultToPublished_andAppearInPublicCatalog() throws Exception {
        LocalDate eventDate = LocalDate.now().plusDays(20);
        String requestJson = String.format("""
                {
                    "title": "Concierto Publicado Auto",
                    "description": "Evento de prueba para publicación inmediata",
                    "date": "%s",
                    "startTime": "19:00:00",
                    "endTime": "23:00:00",
                    "location": "Arena Flow",
                    "capacity": 5000,
                    "categoryId": %d
                }
                """, eventDate, category.getId());

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.title").value("Concierto Publicado Auto"));

        // Public catalog must include the new event
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title", hasItem("Concierto Publicado Auto")));
    }

    @Test
    void createEvent_asDraft_thenPublish_shouldAppearInPublicCatalogAfterPublish() throws Exception {
        LocalDate eventDate = LocalDate.now().plusDays(25);
        String requestJson = String.format("""
                {
                    "title": "Concierto En Borrador",
                    "description": "Evento de prueba que inicia como borrador",
                    "date": "%s",
                    "startTime": "20:00:00",
                    "endTime": "23:30:00",
                    "location": "Teatro Flow",
                    "capacity": 2000,
                    "categoryId": %d,
                    "status": "PENDING"
                }
                """, eventDate, category.getId());

        String responseContent = mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        // Extract ID
        com.fasterxml.jackson.databind.JsonNode rootNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseContent);
        long eventId = rootNode.get("id").asLong();

        // While PENDING, public catalog must NOT show it
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title", not(hasItem("Concierto En Borrador"))));

        // Organizer publishes the event
        mockMvc.perform(patch("/api/events/" + eventId + "/status")
                        .param("status", "PUBLISHED")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isNoContent());

        // Now public catalog MUST show it
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title", hasItem("Concierto En Borrador")));
    }
}
