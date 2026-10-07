package com.fraudshield.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudshield.dto.AuthRequest;
import com.fraudshield.dto.AuthResponse;
import com.fraudshield.dto.RegisterRequest;
import com.fraudshield.entity.Role;
import com.fraudshield.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/login should return JWT token on valid credentials")
    void testLoginSuccess() throws Exception {
        AuthRequest request = new AuthRequest("john.doe@example.com", "User@123");
        AuthResponse response = new AuthResponse(
                "mock-jwt-token-12345",
                "john.doe@example.com",
                "John Doe",
                "ROLE_USER",
                86400000L
        );

        when(authService.login(any(AuthRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token-12345"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("POST /api/auth/register should create user and return 201 Created")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "new.user@example.com",
                "Password@123",
                "New User",
                Role.ROLE_USER
        );
        AuthResponse response = new AuthResponse(
                "mock-jwt-register-token",
                "new.user@example.com",
                "New User",
                "ROLE_USER",
                86400000L
        );

        when(authService.register(any(RegisterRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mock-jwt-register-token"))
                .andExpect(jsonPath("$.email").value("new.user@example.com"));
    }
}
