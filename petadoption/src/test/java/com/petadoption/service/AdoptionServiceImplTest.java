package com.petadoption.service;

import com.petadoption.model.*;
import com.petadoption.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdoptionServiceImplTest {

    @Mock
    private AdoptionRepository adoptionRepository;
    
    @Mock
    private PetRepository petRepository;
    
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdoptionServiceImpl adoptionService;

    private User mockUser;
    private Pet mockPet;
    private Adoption mockAdoption;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        mockPet = new Pet();
        mockPet.setId(1L);
        mockPet.setStatus(PetStatus.AVAILABLE);

        mockAdoption = new Adoption(mockUser, mockPet, AdoptionStatus.PENDING);
    }

    @Test
    void testRequestAdoption_Success() {
        // Mock user and pet repositories
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(petRepository.findById(mockPet.getId())).thenReturn(Optional.of(mockPet));
        when(adoptionRepository.existsByUserAndPet(mockUser, mockPet)).thenReturn(false);
        when(adoptionRepository.save(any(Adoption.class))).thenReturn(mockAdoption);

        // Call method
        Adoption result = adoptionService.requestAdoption(mockPet.getId(), mockUser.getEmail());

        // Verify and assert
        assertNotNull(result);
        assertEquals(mockUser, result.getUser());
        assertEquals(mockPet, result.getPet());
        assertEquals(AdoptionStatus.PENDING, result.getStatus());
        verify(adoptionRepository, times(1)).save(any(Adoption.class));
    }

    @Test
    void testRequestAdoption_UserNotFound() {
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, 
            () -> adoptionService.requestAdoption(mockPet.getId(), mockUser.getEmail()));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testRequestAdoption_PetNotFound() {
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(petRepository.findById(mockPet.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, 
            () -> adoptionService.requestAdoption(mockPet.getId(), mockUser.getEmail()));

        assertEquals("Pet not found", exception.getMessage());
    }

    @Test
    void testRequestAdoption_PetAlreadyAdopted() {
        mockPet.setStatus(PetStatus.ADOPTED);

        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(petRepository.findById(mockPet.getId())).thenReturn(Optional.of(mockPet));

        Exception exception = assertThrows(RuntimeException.class, 
            () -> adoptionService.requestAdoption(mockPet.getId(), mockUser.getEmail()));

        assertEquals("Pet is already adopted", exception.getMessage());
    }

    @Test
    void testRequestAdoption_AlreadyRequested() {
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(petRepository.findById(mockPet.getId())).thenReturn(Optional.of(mockPet));
        when(adoptionRepository.existsByUserAndPet(mockUser, mockPet)).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, 
            () -> adoptionService.requestAdoption(mockPet.getId(), mockUser.getEmail()));

        assertEquals("You have already requested adoption for this pet.", exception.getMessage());
    }

    @Test
    void testGetUserAdoptions_Success() {
        List<Adoption> mockAdoptions = List.of(mockAdoption);

        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(adoptionRepository.findByUser(mockUser)).thenReturn(mockAdoptions);

        List<Adoption> result = adoptionService.getUserAdoptions(mockUser.getEmail());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockAdoption, result.get(0));
    }

    @Test
    void testGetUserAdoptions_UserNotFound() {
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, 
            () -> adoptionService.getUserAdoptions(mockUser.getEmail()));

        assertEquals("User not found", exception.getMessage());
    }
}
