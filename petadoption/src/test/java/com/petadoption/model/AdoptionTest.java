package com.petadoption.model;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdoptionTest {

    @Test
    void testAdoptionCreation() {
        User user = new User("Alice Doe", "alice@example.com", "password123", "USER");
        Pet pet = new Pet(1L, "Buddy", "Dog", "Golden Retriever", 2, PetStatus.AVAILABLE, "/uploads/dog1.jpg");

        Adoption adoption = new Adoption(user, pet, AdoptionStatus.PENDING);

        assertEquals(user, adoption.getUser());
        assertEquals(pet, adoption.getPet());
        assertEquals(AdoptionStatus.PENDING, adoption.getStatus());
    }

    @Test
    void testAdoptionStatusChange() {
        Adoption adoption = new Adoption();
        adoption.setStatus(AdoptionStatus.APPROVED);

        assertEquals(AdoptionStatus.APPROVED, adoption.getStatus());
    }
}
