package com.petadoption.controller;

import com.petadoption.model.Pet;
import com.petadoption.model.PetStatus;
import com.petadoption.repository.AdoptionRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetControllerTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private AdoptionRepository adoptionRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private PetController petController;

    private Pet pet;

    @BeforeEach
    void setUp() {
        pet = new Pet(1L, "Buddy", "Dog", "Golden Retriever", 2, PetStatus.AVAILABLE, "/uploads/dog.jpg");
    }

    @Test
    void testGetAllPets_Success() {
        when(petRepository.findAll()).thenReturn(List.of(pet));

        ResponseEntity<List<Pet>> response = petController.getAllPets();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        assertEquals(pet, response.getBody().get(0));
    }

    @Test
    void testGetPetById_Success() {
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        ResponseEntity<?> response = petController.getPetById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetPetById_NotFound() {
        when(petRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = petController.getPetById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testAddPet_Success() {
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        ResponseEntity<String> response = petController.addPet("Buddy", "Dog", "Golden Retriever", 2, "AVAILABLE", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pet added successfully!", response.getBody());
    }

    @Test
    void testAddPet_WithImage_Success() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.uploadFile(mockFile)).thenReturn("/uploads/dog1.jpg");
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        ResponseEntity<String> response = petController.addPet("Buddy", "Dog", "Golden Retriever", 2, "AVAILABLE", mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pet added successfully!", response.getBody());
    }

    @Test
    void testAddPet_FailedImageUpload() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.uploadFile(mockFile)).thenThrow(new RuntimeException("Upload failed"));

        ResponseEntity<String> response = petController.addPet("Buddy", "Dog", "Golden Retriever", 2, "AVAILABLE", mockFile);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to add pet"));
    }


    @Test
    void testUpdatePet_Success() {
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        ResponseEntity<String> response = petController.updatePet(1L, "Buddy Updated", "Dog", "Golden Retriever", 3, "ADOPTED", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pet updated successfully!", response.getBody());
    }

    @Test
    void testUpdatePet_NotFound() {
        when(petRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = petController.updatePet(1L, "Buddy Updated", "Dog", "Golden Retriever", 3, "ADOPTED", null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Pet not found.", response.getBody());
    }

    @Test
    void testUpdatePet_WithImage_Success()  {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.uploadFile(mockFile)).thenReturn("/uploads/dog.jpg");
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        ResponseEntity<String> response = petController.updatePet(1L, "Buddy Updated", "Dog", "Golden Retriever", 3, "ADOPTED", mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pet updated successfully!", response.getBody());
    }

    @Test
    void testUpdatePet_FailedImageUpload()  {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.uploadFile(mockFile)).thenThrow(new RuntimeException("Upload failed"));
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        ResponseEntity<String> response = petController.updatePet(1L, "Buddy Updated", "Dog", "Golden Retriever", 3, "ADOPTED", mockFile);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to update pet"));
    }

  

}
