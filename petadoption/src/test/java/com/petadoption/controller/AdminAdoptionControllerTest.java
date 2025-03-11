package com.petadoption.controller;

import com.petadoption.model.*;
import com.petadoption.repository.AdoptionRepository;
import com.petadoption.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAdoptionControllerTest {

    @Mock
    private AdoptionRepository adoptionRepository;

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private AdminAdoptionController adminAdoptionController;

    private Adoption adoptionRequest;
    private Pet pet;
    private User user;

    @BeforeEach
    void setUp() {
        // ✅ Mocking a user
        user = new User("John Doe", "john.doe@example.com", "password", "USER");

        // ✅ Mocking a pet
        pet = new Pet(1L, "Buddy", "Dog", "Golden Retriever", 2, PetStatus.AVAILABLE, "/uploads/dog.jpg");

        // ✅ Mocking an adoption request
        adoptionRequest = new Adoption(user, pet, AdoptionStatus.PENDING);
        adoptionRequest.setId(1L);
    }

    // ✅ Test: Get Pending Adoptions (Returns Data)
    @Test
    void testGetPendingAdoptions_WithData() {
        List<Adoption> pendingAdoptions = List.of(adoptionRequest);
        when(adoptionRepository.findByStatus(AdoptionStatus.PENDING)).thenReturn(pendingAdoptions);

        ResponseEntity<List<Adoption>> response = adminAdoptionController.getPendingAdoptions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        verify(adoptionRepository, times(1)).findByStatus(AdoptionStatus.PENDING);
    }

    // ✅ Test: Get Pending Adoptions (No Data)
    @Test
    void testGetPendingAdoptions_NoData() {
        when(adoptionRepository.findByStatus(AdoptionStatus.PENDING)).thenReturn(Collections.emptyList());

        ResponseEntity<List<Adoption>> response = adminAdoptionController.getPendingAdoptions();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(adoptionRepository, times(1)).findByStatus(AdoptionStatus.PENDING);
    }

    // ✅ Test: Approve Adoption (Success)
    @Test
    void testApproveAdoption_Success() {
        when(adoptionRepository.findById(1L)).thenReturn(Optional.of(adoptionRequest));
        when(petRepository.save(any())).thenReturn(pet);
        when(adoptionRepository.save(any())).thenReturn(adoptionRequest);

        ResponseEntity<String> response = adminAdoptionController.approveAdoption(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Adoption approved successfully.", response.getBody());
        assertEquals(PetStatus.ADOPTED, pet.getStatus());
        assertEquals(AdoptionStatus.APPROVED, adoptionRequest.getStatus());

        verify(adoptionRepository, times(1)).findById(1L);
        verify(petRepository, times(1)).save(any(Pet.class));
        verify(adoptionRepository, times(1)).save(any(Adoption.class));
    }

    // ✅ Test: Approve Adoption (Pet Not Available)
    @Test
    void testApproveAdoption_PetNotAvailable() {
        pet.setStatus(PetStatus.ADOPTED); // Simulate already adopted pet

        when(adoptionRepository.findById(1L)).thenReturn(Optional.of(adoptionRequest));

        ResponseEntity<String> response = adminAdoptionController.approveAdoption(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Pet is no longer available for adoption.", response.getBody());

        verify(adoptionRepository, times(1)).findById(1L);
        verify(petRepository, never()).save(any(Pet.class));
        verify(adoptionRepository, never()).save(any(Adoption.class));
    }

    // ✅ Test: Approve Adoption (Request Not Found)
    @Test
    void testApproveAdoption_RequestNotFound() {
        when(adoptionRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = adminAdoptionController.approveAdoption(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Adoption request not found.", response.getBody());

        verify(adoptionRepository, times(1)).findById(1L);
        verify(petRepository, never()).save(any(Pet.class));
        verify(adoptionRepository, never()).save(any(Adoption.class));
    }

    // ✅ Test: Reject Adoption (Success)
    @Test
    void testRejectAdoption_Success() {
        when(adoptionRepository.findById(1L)).thenReturn(Optional.of(adoptionRequest));
        when(adoptionRepository.save(any())).thenReturn(adoptionRequest);

        ResponseEntity<String> response = adminAdoptionController.rejectAdoption(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Adoption request rejected.", response.getBody());
        assertEquals(AdoptionStatus.REJECTED, adoptionRequest.getStatus());

        verify(adoptionRepository, times(1)).findById(1L);
        verify(adoptionRepository, times(1)).save(any(Adoption.class));
    }

    // ✅ Test: Reject Adoption (Already Processed)
    @Test
    void testRejectAdoption_AlreadyProcessed() {
        adoptionRequest.setStatus(AdoptionStatus.APPROVED); // Simulate already processed request

        when(adoptionRepository.findById(1L)).thenReturn(Optional.of(adoptionRequest));

        ResponseEntity<String> response = adminAdoptionController.rejectAdoption(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("This request is already processed.", response.getBody());

        verify(adoptionRepository, times(1)).findById(1L);
        verify(adoptionRepository, never()).save(any(Adoption.class));
    }

    // ✅ Test: Reject Adoption (Request Not Found)
    @Test
    void testRejectAdoption_RequestNotFound() {
        when(adoptionRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = adminAdoptionController.rejectAdoption(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Adoption request not found.", response.getBody());

        verify(adoptionRepository, times(1)).findById(1L);
        verify(adoptionRepository, never()).save(any(Adoption.class));
    }

    // ✅ Test: Get All Adoptions
    @Test
    void testGetAllAdoptions() {
        List<Adoption> adoptions = List.of(adoptionRequest);
        when(adoptionRepository.findLatestAdoptionRecords()).thenReturn(adoptions);

        ResponseEntity<List<Map<String, Object>>> response = adminAdoptionController.getAllAdoptions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());

        Map<String, Object> adoptionData = response.getBody().get(0);
        assertEquals("John Doe", adoptionData.get("adopterName"));
        assertEquals("john.doe@example.com", adoptionData.get("adopterEmail"));
        assertEquals("Buddy", adoptionData.get("petName"));
        assertEquals("Dog", adoptionData.get("petType"));
        assertEquals("Golden Retriever", adoptionData.get("petBreed"));
        assertEquals("PENDING", adoptionData.get("adoptionStatus"));

        verify(adoptionRepository, times(1)).findLatestAdoptionRecords();
    }
}
