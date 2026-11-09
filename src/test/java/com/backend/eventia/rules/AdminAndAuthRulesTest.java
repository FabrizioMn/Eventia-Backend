package com.backend.eventia.rules;

import com.backend.eventia.model.Category;
import com.backend.eventia.model.Order;
import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.OrderStatus;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.CategoryRepository;
import com.backend.eventia.repository.OrderRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminAndAuthRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private String adminToken;

    @BeforeEach
    void setUp() {
        User admin = TestUserFactory.createUser(
                userRepository, passwordEncoder, "admin-rules@test.com", "secret123", UserRole.ADMIN, true, "81000001"
        );
        adminToken = jwtUtils.generateToken(admin);
    }

    @Test
    void registroCumpleYRechazaEmailODniDuplicadoCon409() throws Exception {
        String validBody = "{\"firstName\":\"Ana\",\"lastName\":\"Perez\",\"dni\":\"71234567\","
                + "\"email\":\"ana.perez@test.com\",\"password\":\"secret123\"}";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isCreated());

        // Email duplicado -> 409
        String duplicateEmail = "{\"firstName\":\"Otra\",\"lastName\":\"Perez\",\"dni\":\"79999999\","
                + "\"email\":\"ana.perez@test.com\",\"password\":\"secret123\"}";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateEmail))
                .andExpect(status().isConflict());

        // DNI duplicado -> 409
        String duplicateDni = "{\"firstName\":\"Otra\",\"lastName\":\"Perez\",\"dni\":\"71234567\","
                + "\"email\":\"otra.perez@test.com\",\"password\":\"secret123\"}";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateDni))
                .andExpect(status().isConflict());
    }

    @Test
    void adminOrganizadorCumpleYRechazaEmailODniDuplicadoEnCreacionYEdicion() throws Exception {
        String org1 = "{\"firstName\":\"Carlos\",\"lastName\":\"Rojas\",\"dni\":\"72111222\","
                + "\"email\":\"carlos@org.pe\",\"cellphone\":\"987654321\",\"password\":\"secret123\"}";

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(org1))
                .andExpect(status().isCreated());

        // Crear con email duplicado -> 409
        String dupEmail = "{\"firstName\":\"Luis\",\"lastName\":\"Diaz\",\"dni\":\"72333444\","
                + "\"email\":\"CARLOS@org.pe\",\"cellphone\":\"987654322\",\"password\":\"secret123\"}";
        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dupEmail))
                .andExpect(status().isConflict());

        // Crear con DNI duplicado -> 409
        String dupDni = "{\"firstName\":\"Luis\",\"lastName\":\"Diaz\",\"dni\":\"72111222\","
                + "\"email\":\"luis@org.pe\",\"cellphone\":\"987654322\",\"password\":\"secret123\"}";
        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dupDni))
                .andExpect(status().isConflict());

        // Segundo organizador válido para probar colisión en edición
        User org2 = TestUserFactory.createUser(
                userRepository, passwordEncoder, "org2@org.pe", "secret123", UserRole.ORGANIZER, true, "72555666"
        );

        String updateDupEmail = "{\"firstName\":\"Org2\",\"lastName\":\"Test\",\"dni\":\"72555666\","
                + "\"email\":\"carlos@org.pe\",\"cellphone\":\"987654323\"}";
        mockMvc.perform(put("/api/admin/users/{id}", org2.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateDupEmail))
                .andExpect(status().isConflict());

        String updateDupDni = "{\"firstName\":\"Org2\",\"lastName\":\"Test\",\"dni\":\"72111222\","
                + "\"email\":\"org2@org.pe\",\"cellphone\":\"987654323\"}";
        mockMvc.perform(put("/api/admin/users/{id}", org2.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateDupDni))
                .andExpect(status().isConflict());
    }

    @Test
    void categoriaCumpleYRechazaNombreDuplicadoCon409() throws Exception {
        String createBody = "{\"name\":\" comedia standup \",\"description\":\"Shows\",\"active\":true}";

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Comedia standup"));

        // Duplicado ignorando mayúsculas -> 409
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"COMEDIA STANDUP\",\"description\":\"Otro\",\"active\":true}"))
                .andExpect(status().isConflict());

        Category second = categoryRepository.save(
                Category.builder().name("Deportes").description("Torneos").active(true).build()
        );

        // Actualizar hacia un nombre ya existente -> 409
        mockMvc.perform(put("/api/categories/{id}", second.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Comedia standup\",\"description\":\"Torneos\",\"active\":true}"))
                .andExpect(status().isConflict());
    }

    @Test
    void monitoreoAdminContabilizaSoloOrdenesPagadas() throws Exception {
        orderRepository.deleteAll();
        User buyer = TestUserFactory.createUser(
                userRepository, passwordEncoder, "buyer-kpi@test.com", "secret123", UserRole.USER, true, "81000009"
        );

        orderRepository.save(Order.builder()
                .totalAmount(150.0)
                .paymentMethod("CARD")
                .paymentStatus(OrderStatus.PAID)
                .user(buyer)
                .build());
        orderRepository.save(Order.builder()
                .totalAmount(999.0)
                .paymentMethod("CARD")
                .paymentStatus(OrderStatus.CANCELLED)
                .user(buyer)
                .build());

        mockMvc.perform(get("/api/admin/monitoring/kpis")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue.value").value(150.0))
                .andExpect(jsonPath("$.processedOrders.value").value(1.0));
    }
}
