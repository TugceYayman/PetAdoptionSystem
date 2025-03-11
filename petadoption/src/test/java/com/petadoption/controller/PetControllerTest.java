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
import org.mockito.Mockito;
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

    // ✅ Test: Get All Pets (Success)
    @Test
    void testGetAllPets_Success() {
        when(petRepository.findAll()).thenReturn(List.of(pet));

        ResponseEntity<List<Pet>> response = petController.getAllPets();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        assertEquals(pet, response.getBody().get(0));
    }

    // ✅ Test: Get Pet by ID (Success)
    @Test
    void testGetPetById_Success() {
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        ResponseEntity<?> response = petController.getPetById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ✅ Test: Get Pet by ID (Not Found)
    @Test
    void testGetPetById_NotFound() {
        when(petRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = petController.getPetById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ✅ Test: Add Pet (Success)
    @Test
    void testAddPet_Success() {
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        ResponseEntity<String> response = petController.addPet("Buddy", "Dog", "Golden Retriever", 2, "AVAILABLE", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pet added successfully!", response.getBody());
    }

    // ✅ Test: Add Pet (With Image Upload)
    @Test
    void testAddPet_WithImage_Success() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.uploadFile(mockFile)).thenReturn("/uploads/dog.jpg");
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        ResponseEntity<String> response = petController.addPet("Buddy", "Dog", "Golden Retriever", 2, "AVAILABLE", mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pet added successfully!", response.getBody());
    }

    // ✅ Test: Add Pet (Failed Image Upload)
    @Test
    void testAddPet_FailedImageUpload() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.uploadFile(mockFile)).thenThrow(new RuntimeException("Upload failed"));

        ResponseEntity<String> response = petController.addPet("Buddy", "Dog", "Golden Retriever", 2, "AVAILABLE", mockFile);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to add pet"));
    }

    // ✅ Test: Update Pet (Success)
    @Test
    void testUpdatePet_Success() {
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        ResponseEntity<String> response = petController.updatePet(1L, "Buddy Updated", "Dog", "Golden Retriever", 3, "ADOPTED", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pet updated successfully!", response.getBody());
    }

    // ✅ Test: Update Pet (Not Found)
    @Test
    void testUpdatePet_NotFound() {
        when(petRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = petController.updatePet(1L, "Buddy Updated", "Dog", "Golden Retriever", 3, "ADOPTED", null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Pet not found.", response.getBody());
    }

    // ✅ Test: Update Pet (With Image Upload)
    @Test
    void testUpdatePet_WithImage_Success() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.uploadFile(mockFile)).thenReturn("/uploads/dog.jpg");
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        ResponseEntity<String> response = petController.updatePet(1L, "Buddy Updated", "Dog", "Golden Retriever", 3, "ADOPTED", mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pet updated successfully!", response.getBody());
    }

    // ✅ Test: Update Pet (Failed Image Upload)
    @Test
    void testUpdatePet_FailedImageUpload() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.uploadFile(mockFile)).thenThrow(new RuntimeException("Upload failed"));
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        ResponseEntity<String> response = petController.updatePet(1L, "Buddy Updated", "Dog", "Golden Retriever", 3, "ADOPTED", mockFile);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to update pet"));
    }

    // ✅ Test: Delete Pet (Success)
    @Test
    void testDeletePet_Success() {
        when(petRepository.existsById(1L)).thenReturn(true);
        doNothing().when(adoptionRepository).deleteByPetId(1L);
        doNothing().when(petRepository).deleteById(1L);

        ResponseEntity<String> response = petController.deletePet(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("✅ Pet deleted successfully!", response.getBody());
    }

    // ✅ Test: Delete Pet (Not Found)
    @Test
    void testDeletePet_NotFound() {
        when(petRepository.existsById(1L)).thenReturn(false);

        ResponseEntity<String> response = petController.deletePet(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Pet not found.", response.getBody());
    }

    // ✅ Test: Delete Pet (Failure)
    @Test
    void testDeletePet_Failure() {
        when(petRepository.existsById(1L)).thenReturn(true);
        doThrow(new RuntimeException("Delete failed")).when(petRepository).deleteById(1L);

        ResponseEntity<String> response = petController.deletePet(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to delete pet"));
    }
}
