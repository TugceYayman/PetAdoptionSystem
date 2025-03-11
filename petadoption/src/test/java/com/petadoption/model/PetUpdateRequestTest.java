package com.petadoption.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.mock.web.MockMultipartFile;

class PetUpdateRequestTest {

    @Test
    void testPetUpdateRequest() {
        MockMultipartFile mockImage = new MockMultipartFile("image", "dog.jpg", "image/jpeg", new byte[10]);

        PetUpdateRequest request = new PetUpdateRequest();
        request.setName("Charlie");
        request.setType("Dog");
        request.setBreed("Labrador");
        request.setAge(3);
        request.setStatus("AVAILABLE");
        request.setImage(mockImage);

        assertEquals("Charlie", request.getName());
        assertEquals("Dog", request.getType());
        assertEquals("Labrador", request.getBreed());
        assertEquals(3, request.getAge());
        assertEquals("AVAILABLE", request.getStatus());
        assertNotNull(request.getImage());
    }
}
