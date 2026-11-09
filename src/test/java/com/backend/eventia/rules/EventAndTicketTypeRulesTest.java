package com.backend.eventia.rules;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EventAndTicketTypeRulesTest {

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

    private User organizerA;
    private User organizerB;
    private String tokenOrgA;
    private String tokenOrgB;
    private Category activeCategory;
    private Category inactiveCategory;

    @BeforeEach
    void setUp() {
        organizerA = TestUserFactory.createUser(
                userRepository, passwordEncoder, "orga-rules@test.com", "secret123", UserRole.ORGANIZER, true, "82000001"
        );
        organizerB = TestUserFactory.createUser(
                userRepository, passwordEncoder, "orgb-rules@test.com", "secret123", UserRole.ORGANIZER, true, "82000002"
        );
        tokenOrgA = jwtUtils.generateToken(organizerA);
        tokenOrgB = jwtUtils.generateToken(organizerB);

        activeCategory = categoryRepository.save(
                Category.builder().name("Cat Activa").description("d").active(true).build()
        );
        inactiveCategory = categoryRepository.save(
                Category.builder().name("Cat Inactiva").description("d").active(false).build()
        );
    }

    @Test
    void eventoRechazaFechaPasadaYHorarioInvalidoCon400() throws Exception {
        String pastDate = LocalDate.now().minusDays(1).toString();
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + tokenOrgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Evento Pasado", pastDate, "18:00:00", "20:00:00", activeCategory.getId(), "DRAFT")))
                .andExpect(status().isBadRequest());

        String futureDate = LocalDate.now().plusDays(7).toString();
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + tokenOrgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Horario Malo", futureDate, "20:00:00", "18:00:00", activeCategory.getId(), "DRAFT")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eventoRechazaTituloDuplicadoMismoOrganizadorPeroPermiteEnOtro() throws Exception {
        String futureDate = LocalDate.now().plusDays(10).toString();
        String body = eventJson("Concierto Unico", futureDate, "19:00:00", "22:00:00", activeCategory.getId(), "DRAFT");

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + tokenOrgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // Mismo organizador -> 409
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + tokenOrgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());

        // Distinto organizador -> 201
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + tokenOrgB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void eventoRechazaCategoriaInactivaCon400YCategoriaInexistenteCon404() throws Exception {
        String futureDate = LocalDate.now().plusDays(10).toString();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + tokenOrgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Con Cat Inactiva", futureDate, "18:00:00", "20:00:00", inactiveCategory.getId(), "DRAFT")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + tokenOrgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Con Cat Inexistente", futureDate, "18:00:00", "20:00:00", 999999L, "DRAFT")))
                .andExpect(status().isNotFound());
    }

    @Test
    void eventoValidaEstadosEnCreacionYTransicion() throws Exception {
        String futureDate = LocalDate.now().plusDays(10).toString();

        // La creación siempre inicia en PENDING; el DTO no acepta estado del cliente.
        Event cancelledEvent = eventRepository.save(Event.builder()
                .title("Ya Cancelado")
                .description("d")
                .date(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .location("Lima")
                .capacity(100)
                .status(EventStatus.CANCELLED)
                .active(true)
                .category(activeCategory)
                .organizer(organizerA)
                .build());

        // Intentar pasar un evento CANCELLED a PUBLISHED -> 409
        mockMvc.perform(patch("/api/events/{id}/status", cancelledEvent.getId())
                        .header("Authorization", "Bearer " + tokenOrgA)
                        .param("status", "PUBLISHED"))
                .andExpect(status().isConflict());
    }

    @Test
    void tarifasValidanAforoMaximoReduccionNombreUnicoYFechasDeVenta() throws Exception {
        LocalDate eventDate = LocalDate.now().plusDays(15);
        Event event = eventRepository.save(Event.builder()
                .title("Evento Aforo")
                .description("d")
                .date(eventDate)
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(22, 0))
                .location("Lima")
                .capacity(100)
                .status(EventStatus.PUBLISHED)
                .active(true)
                .category(activeCategory)
                .organizer(organizerA)
                .build());

        // 1. Crear tarifa dentro de capacidad (60/100) -> 201
        mockMvc.perform(post("/api/events/{eventId}/ticket-types", event.getId())
                        .header("Authorization", "Bearer " + tokenOrgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"VIP\",\"zone\":\"VIP\",\"price\":100.0,\"totalCapacity\":60}"))
                .andExpect(status().isCreated());

        // 2. Nombre de tarifa duplicado en el mismo evento -> 409
        mockMvc.perform(post("/api/events/{eventId}/ticket-types", event.getId())
                        .header("Authorization", "Bearer " + tokenOrgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"vip\",\"zone\":\"VIP\",\"price\":100.0,\"totalCapacity\":10}"))
                .andExpect(status().isConflict());

        // 3. Superar capacidad del evento (60 + 50 > 100) -> 400
        mockMvc.perform(post("/api/events/{eventId}/ticket-types", event.getId())
                        .header("Authorization", "Bearer " + tokenOrgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"General\",\"zone\":\"Gen\",\"price\":50.0,\"totalCapacity\":50}"))
                .andExpect(status().isBadRequest());

        // 4. Fechas de venta inválidas (fin antes que inicio o después del evento) -> 400
        String start = LocalDate.now().plusDays(5).toString();
        String badEnd = LocalDate.now().plusDays(2).toString();
        mockMvc.perform(post("/api/events/{eventId}/ticket-types", event.getId())
                        .header("Authorization", "Bearer " + tokenOrgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Preventa\",\"zone\":\"Gen\",\"price\":40.0,\"totalCapacity\":20,"
                                + "\"saleStartDate\":\"" + start + "\",\"saleEndDate\":\"" + badEnd + "\"}"))
                .andExpect(status().isBadRequest());

        String afterEvent = eventDate.plusDays(2).toString();
        mockMvc.perform(post("/api/events/{eventId}/ticket-types", event.getId())
                        .header("Authorization", "Bearer " + tokenOrgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tardia\",\"zone\":\"Gen\",\"price\":40.0,\"totalCapacity\":20,"
                                + "\"saleStartDate\":\"" + start + "\",\"saleEndDate\":\"" + afterEvent + "\"}"))
                .andExpect(status().isBadRequest());

        // 5. No reducir aforo por debajo de vendidos (total=40, disp=10 => vendidos=30)
        TicketType withSales = ticketTypeRepository.save(TicketType.builder()
                .name("ConVentas")
                .zone("Platea")
                .price(80.0)
                .totalCapacity(40)
                .availableCapacity(10)
                .maxPerPurchase(6)
                .isPresale(false)
                .active(true)
                .event(event)
                .build());

        // Reducir a 25 (< 30 vendidos) -> 400
        mockMvc.perform(put("/api/ticket-types/{id}", withSales.getId())
                        .header("Authorization", "Bearer " + tokenOrgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"ConVentas\",\"zone\":\"Platea\",\"price\":80.0,\"totalCapacity\":25}"))
                .andExpect(status().isBadRequest());

        // Reducir a 35 (>= 30 vendidos) -> 200
        mockMvc.perform(put("/api/ticket-types/{id}", withSales.getId())
                        .header("Authorization", "Bearer " + tokenOrgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"ConVentas\",\"zone\":\"Platea\",\"price\":80.0,\"totalCapacity\":35}"))
                .andExpect(status().isOk());
    }

    private String eventJson(String title, String date, String startTime, String endTime, Long categoryId, String status) {
        return "{\"title\":\"" + title + "\",\"description\":\"desc\",\"date\":\"" + date + "\","
                + "\"startTime\":\"" + startTime + "\",\"endTime\":\"" + endTime + "\",\"location\":\"Lima\","
                + "\"capacity\":100,\"categoryId\":" + categoryId + ",\"status\":\"" + status + "\"}";
    }
}
