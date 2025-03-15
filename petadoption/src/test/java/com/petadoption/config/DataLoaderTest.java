package com.petadoption.config;

import com.petadoption.model.Pet;
import com.petadoption.model.PetStatus;
import com.petadoption.model.User;
import com.petadoption.repository.PetRepository;
import com.petadoption.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataLoaderTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataLoader dataLoader;

    @Captor
    private ArgumentCaptor<List<Pet>> petListCaptor;

    private final String ADMIN_PASSWORD = "testpassword";

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    }

    @Test
    void testInitData_ShouldLoadSamplePets_WhenNoPetsExist() {
        // Arrange
        when(petRepository.count()).thenReturn(0L);

        // Act
        try {
			dataLoader.initData(petRepository, userRepository, passwordEncoder, ADMIN_PASSWORD).run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Assert
        verify(petRepository, times(1)).saveAll(petListCaptor.capture());
        List<Pet> savedPets = petListCaptor.getValue();
        assertFalse(savedPets.isEmpty());
        assertEquals(13, savedPets.size());
        assertEquals("Buddy", savedPets.get(0).getName());
    }

    @Test
    void testInitData_ShouldNotLoadSamplePets_WhenPetsAlreadyExist() {
        // Arrange
        when(petRepository.count()).thenReturn(10L);

        // Act
        try {
			dataLoader.initData(petRepository, userRepository, passwordEncoder, ADMIN_PASSWORD).run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Assert
        verify(petRepository, never()).saveAll(anyList());
    }

    @Test
    void testInitData_ShouldCreateAdminUser_WhenAdminDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail("admin@petadoption.com")).thenReturn(Optional.empty());

        // Act
        try {
			dataLoader.initData(petRepository, userRepository, passwordEncoder, ADMIN_PASSWORD).run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedAdmin = userCaptor.getValue();
        assertEquals("admin@petadoption.com", savedAdmin.getEmail());
        assertEquals("ADMIN", savedAdmin.getRole());
        assertEquals("encodedPassword", savedAdmin.getPassword());
    }

}
