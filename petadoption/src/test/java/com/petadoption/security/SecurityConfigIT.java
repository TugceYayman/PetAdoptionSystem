package com.petadoption.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SecurityConfigIT {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Test
    void testPasswordEncoder() {
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder.encode("password").length() > 0);
    }

    @Test
    void testAuthenticationManager() {
        assertNotNull(authenticationManager);
    }
}
