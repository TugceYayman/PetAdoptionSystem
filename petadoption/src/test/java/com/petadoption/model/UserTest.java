package com.petadoption.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserCreation() {
        User user = new User("Alice Doe", "alice@example.com", "password123", "USER");

        assertEquals("Alice Doe", user.getName());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("USER", user.getRole());
    }

    @Test
    void testSetUserDetails() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("securepass");
        user.setRole("ADMIN");

        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("securepass", user.getPassword());
        assertEquals("ADMIN", user.getRole());
    }
}
