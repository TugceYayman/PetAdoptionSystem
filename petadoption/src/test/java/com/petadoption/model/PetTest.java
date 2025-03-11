package com.petadoption.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PetTest {

    @Test
    void testPetCreation() {
        Pet pet = new Pet(1L, "Buddy", "Dog", "Golden Retriever", 2, PetStatus.AVAILABLE, "/uploads/dog1.jpg");

        assertEquals(1L, pet.getId());
        assertEquals("Buddy", pet.getName());
        assertEquals("Dog", pet.getType());
        assertEquals("Golden Retriever", pet.getBreed());
        assertEquals(2, pet.getAge());
        assertEquals(PetStatus.AVAILABLE, pet.getStatus());
        assertEquals("/uploads/dog1.jpg", pet.getImageUrl());
    }

    @Test
    void testPetStatusChange() {
        Pet pet = new Pet();
        pet.setStatus(PetStatus.ADOPTED);

        assertEquals(PetStatus.ADOPTED, pet.getStatus());
    }
}
