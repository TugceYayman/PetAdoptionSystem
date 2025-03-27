package com.petadoption.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petadoption.exception.InvalidCredentialsException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerTest implements ApplicationContextAware {

    private ApplicationContext context;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }

    @BeforeAll
    void setUp() {
        this.mockMvc = context.getBean(MockMvc.class);
        this.objectMapper = context.getBean(ObjectMapper.class);
    }

    @Test
    void testLoginSuccess() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setEmail("admin@petadoption.com");
        request.setPassword("admin123");

        String jsonResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode responseNode = objectMapper.readTree(jsonResponse);

        assertTrue(responseNode.hasNonNull("token"), "Response should contain a token");
        assertThat(responseNode.get("token").asText(), not(emptyOrNullString()));
        assertEquals("ADMIN", responseNode.get("role").asText(), "Expected role to be ADMIN");
    }



    @Test
    void testRegisterUser_EmailAlreadyExists() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setName("Admin");  
        request.setEmail("admin@petadoption.com");  
        request.setPassword("password123");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(result -> {
                String jsonResponse = result.getResponse().getContentAsString();
                JsonNode responseNode = objectMapper.readTree(jsonResponse);
                assertEquals("Email already exists", responseNode.get("message").asText());
            });
    }



    private static class AuthRequest {
        private String name;  
        private String email;
        private String password;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
