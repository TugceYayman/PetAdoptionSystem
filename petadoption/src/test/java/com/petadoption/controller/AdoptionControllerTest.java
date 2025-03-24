package com.petadoption.controller;

import com.petadoption.model.*;
import com.petadoption.repository.AdoptionRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdoptionControllerTest {

    @Mock
    private AdoptionRepository adoptionRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AdoptionController adoptionController;

    private User user;
    private Pet pet;
    private Adoption adoption;

    @BeforeEach
    void setUp() {
        user = new User("John Doe", "john.doe@example.com", "password", "USER");
        user.setId(1L);

        pet = new Pet(1L, "Buddy", "Dog", "Golden Retriever", 2, PetStatus.AVAILABLE, "/uploads/dog.jpg");

        adoption = new Adoption(user, pet, AdoptionStatus.PENDING);
        adoption.setId(1L);

        when(authentication.getName()).thenReturn(user.getEmail());
    }

    @Test
    void testRequestAdoption_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(adoptionRepository.existsByUserAndPetAndStatusIn(any(), any(), any())).thenReturn(false);

        ResponseEntity<String> response = adoptionController.requestAdoption(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Adoption request sent successfully.", response.getBody());
        verify(adoptionRepository, times(1)).save(any(Adoption.class));
    }

    @Test
    void testRequestAdoption_UserNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                adoptionController.requestAdoption(1L, authentication));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found.", exception.getReason());
    }

    @Test
    void testRequestAdoption_PetNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(petRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                adoptionController.requestAdoption(1L, authentication));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Pet not found.", exception.getReason());
    }

    @Test
    void testRequestAdoption_PetAlreadyAdopted() {
        pet.setStatus(PetStatus.ADOPTED);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        ResponseEntity<String> response = adoptionController.requestAdoption(1L, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Pet is already adopted.", response.getBody());
    }

    @Test
    void testGetMyAdoptedPets_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(adoptionRepository.findByUserAndStatus(user, AdoptionStatus.APPROVED)).thenReturn(List.of(adoption));

        ResponseEntity<List<Pet>> response = adoptionController.getMyAdoptedPets(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        assertEquals(pet, response.getBody().get(0));
    }

    @Test
    void testGetMyAdoptedPets_UserNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                adoptionController.getMyAdoptedPets(authentication));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found.", exception.getReason());
    }

    @Test
    void testUnadoptPet_Success() {
        pet.setStatus(PetStatus.ADOPTED);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(adoptionRepository.findByUserAndPetAndStatus(user, pet, AdoptionStatus.APPROVED)).thenReturn(Optional.of(adoption));

        ResponseEntity<String> response = adoptionController.unadoptPet(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pet successfully un-adopted.", response.getBody());

        verify(petRepository, times(1)).save(any(Pet.class));
        verify(adoptionRepository, times(1)).delete(any(Adoption.class));
    }

    @Test
    void testUnadoptPet_UserNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                adoptionController.unadoptPet(1L, authentication));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found.", exception.getReason());
    }

    @Test
    void testUnadoptPet_PetNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(petRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                adoptionController.unadoptPet(1L, authentication));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Pet not found.", exception.getReason());
    }

    @Test
    void testUnadoptPet_AdoptionRecordNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(adoptionRepository.findByUserAndPetAndStatus(user, pet, AdoptionStatus.APPROVED)).thenReturn(Optional.empty());

        ResponseEntity<String> response = adoptionController.unadoptPet(1L, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Adoption record not found or pet is not adopted.", response.getBody());
    }

    @Test
    void testUnadoptPet_PetNotAdopted() {
        pet.setStatus(PetStatus.AVAILABLE);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(adoptionRepository.findByUserAndPetAndStatus(user, pet, AdoptionStatus.APPROVED)).thenReturn(Optional.of(adoption));

        ResponseEntity<String> response = adoptionController.unadoptPet(1L, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Pet is not currently adopted.", response.getBody());
    }
}
