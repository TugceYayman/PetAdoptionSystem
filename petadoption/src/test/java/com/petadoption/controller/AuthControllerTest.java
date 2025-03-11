package com.petadoption.controller;

import com.petadoption.model.User;
import com.petadoption.repository.UserRepository;
import com.petadoption.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("John Doe", "john@example.com", "password123", "USER");
    }

    // ✅ Test: Successful User Registration
    @Test
    void testRegisterUser_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");

        ResponseEntity<Map<String, String>> response = authController.register(user);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody().get("message"));
        verify(userRepository, times(1)).save(any(User.class));  // ✅ Ensure user is saved
    }

    // ❌ Test: Registration Fails Due to Existing Email
    @Test
    void testRegisterUser_EmailAlreadyExists() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        ResponseEntity<Map<String, String>> response = authController.register(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already exists", response.getBody().get("message"));
        verify(userRepository, never()).save(any(User.class));  // 🚨 Ensure user is NOT saved
    }

    // ✅ Test: Successful Login
    @Test
    void testLoginUser_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(user.getEmail(), user.getRole())).thenReturn("mocked-jwt-token");

        Map<String, String> response = authController.login(Map.of("email", user.getEmail(), "password", "password123"));

        assertNotNull(response.get("token"));
        assertEquals("mocked-jwt-token", response.get("token"));
        assertEquals("USER", response.get("role"));
    }

    // ❌ Test: Login Fails Due to Invalid Credentials
    @Test
    void testLoginUser_Failure() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () ->
                authController.login(Map.of("email", user.getEmail(), "password", "wrongPassword")));

        assertEquals("Invalid credentials", exception.getMessage());
    }

    // ❌ Test: Login Fails Due to Nonexistent Email
    @Test
    void testLoginUser_EmailNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
                authController.login(Map.of("email", "nonexistent@example.com", "password", "password123")));

        assertEquals("Invalid credentials", exception.getMessage());
    }
}
