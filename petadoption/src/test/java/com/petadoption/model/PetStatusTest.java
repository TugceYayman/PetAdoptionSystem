package com.petadoption.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PetStatusTest {

    @Test
    void testPetStatusValues() {
        assertEquals("AVAILABLE", PetStatus.AVAILABLE.name());
        assertEquals("ADOPTED", PetStatus.ADOPTED.name());
    }
}
