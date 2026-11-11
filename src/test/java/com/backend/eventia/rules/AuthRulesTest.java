package com.backend.eventia.rules;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthRulesTest {

    @Autowired
    private MockMvc mockMvc;

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
}
