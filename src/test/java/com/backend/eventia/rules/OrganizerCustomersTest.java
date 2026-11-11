package com.backend.eventia.rules;

import com.backend.eventia.model.Category;
import com.backend.eventia.model.Event;
import com.backend.eventia.model.Order;
import com.backend.eventia.model.OrderDetail;
import com.backend.eventia.model.TicketType;
import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.EventStatus;
import com.backend.eventia.model.enums.OrderStatus;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.CategoryRepository;
import com.backend.eventia.repository.EventRepository;
import com.backend.eventia.repository.OrderRepository;
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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrganizerCustomersTest {

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
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private String organizer1Token;
    private String organizer2Token;
    private String clientToken;

    @BeforeEach
    void setUp() {
        User organizer1 = TestUserFactory.createUser(userRepository, passwordEncoder,
                "org1-lista@test.com", "secret123", UserRole.ORGANIZER, true, "74000001");
        User organizer2 = TestUserFactory.createUser(userRepository, passwordEncoder,
                "org2-lista@test.com", "secret123", UserRole.ORGANIZER, true, "74000002");
        User client1 = TestUserFactory.createUser(userRepository, passwordEncoder,
                "cli1@test.com", "secret123", UserRole.USER, true, "74000003");
        User client2 = TestUserFactory.createUser(userRepository, passwordEncoder,
                "cli2@test.com", "secret123", UserRole.USER, true, "74000004");
        User inactive = TestUserFactory.createUser(userRepository, passwordEncoder,
                "inactivo-lista@test.com", "secret123", UserRole.USER, false, "74000005");

        client1.setFirstName("Ana");
        client2.setFirstName("Beto");
        inactive.setFirstName("Ines");
        userRepository.save(client1);
        userRepository.save(client2);
        userRepository.save(inactive);

        organizer1Token = jwtUtils.generateToken(organizer1);
        organizer2Token = jwtUtils.generateToken(organizer2);
        clientToken = jwtUtils.generateToken(client1);

        Category category = categoryRepository.save(
                Category.builder().name("Cat Lista").description("d").active(true).build());

        TicketType ticketType1 = createEventWithTicketType("Evento Org 1", category, organizer1);
        TicketType ticketType2 = createEventWithTicketType("Evento Org 2", category, organizer2);

        createOrder(client1, ticketType1, 2);
        createOrder(client2, ticketType2, 3);
        createOrder(inactive, ticketType1, 1);
    }

    @Test
    void organizadorVeSoloSusCompradores() throws Exception {
        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + organizer1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[*].email", hasItems("cli1@test.com", "inactivo-lista@test.com")))
                .andExpect(jsonPath("$.content[*].email", not(hasItem("cli2@test.com"))))
                .andExpect(jsonPath("$.content[0].ticketsPurchased").value(2));

        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + organizer2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("cli2@test.com"))
                .andExpect(jsonPath("$.content[0].ticketsPurchased").value(3));
    }

    @Test
    void buscaCompradoresPorTexto() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("q", "cli1@test.com")
                        .header("Authorization", "Bearer " + organizer1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("cli1@test.com"));
    }

    @Test
    void filtraCompradoresPorEstadoActivo() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("active", "false")
                        .header("Authorization", "Bearer " + organizer1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("inactivo-lista@test.com"))
                .andExpect(jsonPath("$.content[0].active").value(false));
    }

    @Test
    void clienteNoPuedeConsultarUsuarios() throws Exception {
        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    private TicketType createEventWithTicketType(String title, Category category, User organizer) {
        Event event = eventRepository.save(Event.builder()
                .title(title)
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
        return ticketTypeRepository.save(TicketType.builder()
                .name("General")
                .zone("General")
                .price(50.0)
                .totalCapacity(100)
                .availableCapacity(100)
                .maxPerPurchase(6)
                .isPresale(false)
                .active(true)
                .event(event)
                .build());
    }

    private void createOrder(User user, TicketType ticketType, int quantity) {
        double subtotal = ticketType.getPrice() * quantity;
        Order order = Order.builder()
                .totalAmount(subtotal)
                .paymentMethod("CARD")
                .paymentStatus(OrderStatus.PAID)
                .user(user)
                .build();
        OrderDetail detail = OrderDetail.builder()
                .quantity(quantity)
                .unitPrice(ticketType.getPrice())
                .subtotal(subtotal)
                .order(order)
                .ticketType(ticketType)
                .build();
        order.getOrderDetails().add(detail);
        orderRepository.save(order);
    }
}
