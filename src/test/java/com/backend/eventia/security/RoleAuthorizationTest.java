package com.backend.eventia.security;

import com.backend.eventia.dto.category.CategoryRequestDto;
import com.backend.eventia.dto.event.EventRequestDto;
import com.backend.eventia.model.Category;
import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.EventStatus;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.CategoryRepository;
import com.backend.eventia.repository.UserRepository;
import com.backend.eventia.support.TestUserFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoleAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private String adminToken;
    private String organizerToken;
    private String userToken;
    private Category category;

    @BeforeEach
    void setUp() {
        User admin = TestUserFactory.createUser(userRepository, passwordEncoder,
                "admin-role@test.com", "secret123", UserRole.ADMIN, true);
        User organizer = TestUserFactory.createUser(userRepository, passwordEncoder,
                "org-role@test.com", "secret123", UserRole.ORGANIZER, true);
        User user = TestUserFactory.createUser(userRepository, passwordEncoder,
                "user-role@test.com", "secret123", UserRole.USER, true);
        adminToken = jwtUtils.generateToken(admin);
        organizerToken = jwtUtils.generateToken(organizer);
        userToken = jwtUtils.generateToken(user);
        category = categoryRepository.save(Category.builder().name("Test Cat").description("d").active(true).build());
    }

    @Test
    void userNoPuedeAccederAMonitoringAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/monitoring").header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizerNoPuedeAccederAAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/monitoring").header("Authorization", bearer(organizerToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeAccederAMonitoring() throws Exception {
        mockMvc.perform(get("/api/admin/monitoring").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
    }

    @Test
    void userNoPuedeCrearEventosYOrganizerSi() throws Exception {
        EventRequestDto request = new EventRequestDto("Evento Test", "desc", LocalDate.now().plusDays(10),
                LocalTime.of(18, 0), LocalTime.of(20, 0), "Lima", 100, category.getId(), null, EventStatus.DRAFT);
        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/events").header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/events").header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void organizerNoPuedeCrearCategoriaYAdminSi() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto("Nueva Cat", "desc");

        mockMvc.perform(post("/api/categories").header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/categories").header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequestDto("Otra Cat", "desc"))))
                .andExpect(status().isCreated());
    }

    @Test
    void accesoADashboardOrganizerSegunRol() throws Exception {
        mockMvc.perform(get("/api/organizer/dashboard").header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/organizer/dashboard").header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk());
    }

    @Test
    void sinTokenAEndpointProtegidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/admin/monitoring"))
                .andExpect(status().isUnauthorized());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
