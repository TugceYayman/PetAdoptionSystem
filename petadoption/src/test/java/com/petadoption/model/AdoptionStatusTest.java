package com.petadoption.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdoptionStatusTest {

    @Test
    void testAdoptionStatusValues() {
        assertEquals("PENDING", AdoptionStatus.PENDING.name());
        assertEquals("APPROVED", AdoptionStatus.APPROVED.name());
        assertEquals("REJECTED", AdoptionStatus.REJECTED.name());
    }
}
