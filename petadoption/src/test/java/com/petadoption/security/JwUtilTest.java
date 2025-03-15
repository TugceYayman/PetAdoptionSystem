package com.petadoption.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String testToken;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        testToken = jwtUtil.generateToken("user@petadoption.com", "USER");
    }

    @Test
    void generateToken_Valid() {
        assertNotNull(testToken);
    }

    @Test
    void validateToken_Valid() {
        assertTrue(jwtUtil.validateToken(testToken));
    }

    @Test
    void validateToken_Invalid() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void getUsernameFromToken() {
        String username = jwtUtil.getUsernameFromToken(testToken);
        assertEquals("user@petadoption.com", username);
    }

    @Test
    void getRolesFromToken() {
        List<String> roles = jwtUtil.getRolesFromToken(testToken);
        assertTrue(roles.contains("USER"));
    }
}
