package com.backend.eventia.tickets;

import com.backend.eventia.model.Category;
import com.backend.eventia.model.Event;
import com.backend.eventia.model.Order;
import com.backend.eventia.model.OrderDetail;
import com.backend.eventia.model.TicketType;
import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.EventStatus;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.CategoryRepository;
import com.backend.eventia.repository.EventRepository;
import com.backend.eventia.repository.OrderDetailRepository;
import com.backend.eventia.repository.OrderRepository;
import com.backend.eventia.repository.TicketTypeRepository;
import com.backend.eventia.repository.UserRepository;
import com.backend.eventia.security.JwtUtils;
import com.backend.eventia.support.TestUserFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TicketTypeDeletionTest {

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
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void borrarTarifaConVentasDevuelve409() throws Exception {
        User organizer = TestUserFactory.createUser(userRepository, passwordEncoder,
                "org-ticket@test.com", "secret123", UserRole.ORGANIZER, true);
        User buyer = TestUserFactory.createUser(userRepository, passwordEncoder,
                "buyer-ticket@test.com", "secret123", UserRole.USER, true);
        Category category = categoryRepository.save(Category.builder().name("Cat Ticket").description("d").active(true).build());
        Event event = eventRepository.save(Event.builder()
                .title("Evento Ticket")
                .description("d")
                .date(LocalDate.now().plusDays(5))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .location("Lima")
                .capacity(100)
                .status(EventStatus.PUBLISHED)
                .active(true)
                .category(category)
                .organizer(organizer)
                .build());
        TicketType ticketType = ticketTypeRepository.save(TicketType.builder()
                .name("General")
                .zone("General")
                .price(50.0)
                .totalCapacity(100)
                .availableCapacity(99)
                .maxPerPurchase(6)
                .isPresale(false)
                .active(true)
                .event(event)
                .build());
        Order order = orderRepository.save(Order.builder()
                .totalAmount(50.0)
                .paymentMethod("CARD")
                .paymentStatus("PAGADO")
                .user(buyer)
                .build());
        orderDetailRepository.save(OrderDetail.builder()
                .quantity(1)
                .unitPrice(50.0)
                .subtotal(50.0)
                .order(order)
                .ticketType(ticketType)
                .build());

        String token = jwtUtils.generateToken(organizer);
        mockMvc.perform(delete("/api/ticket-types/{id}", ticketType.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }
}
