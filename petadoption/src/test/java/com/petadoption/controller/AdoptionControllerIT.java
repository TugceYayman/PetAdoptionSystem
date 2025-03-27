package com.petadoption.controller;

import com.petadoption.model.*;
import com.petadoption.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdoptionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private AdoptionRepository adoptionRepository;

    private User testUser;
    private Pet availablePet;
    private Pet adoptedPet;
    private Adoption pendingAdoption;
    private Adoption approvedAdoption;

    @BeforeEach
    void setUp() {
        // Create test user with all required fields
        testUser = new User();
        testUser.setEmail("test@user.com");
        testUser.setPassword("password");
        testUser.setName("Test User");
        testUser.setRole("ROLE_USER"); // Changed to ROLE_USER format
        testUser = userRepository.save(testUser);

        // Create test pets
        availablePet = new Pet();
        availablePet.setName("Available Pet");
        availablePet.setStatus(PetStatus.AVAILABLE);
        availablePet = petRepository.save(availablePet);

        adoptedPet = new Pet();
        adoptedPet.setName("Adopted Pet");
        adoptedPet.setStatus(PetStatus.ADOPTED);
        adoptedPet = petRepository.save(adoptedPet);

        // Create test adoptions
        pendingAdoption = new Adoption(testUser, availablePet, AdoptionStatus.PENDING);
        pendingAdoption = adoptionRepository.save(pendingAdoption);

        approvedAdoption = new Adoption(testUser, adoptedPet, AdoptionStatus.APPROVED);
        approvedAdoption = adoptionRepository.save(approvedAdoption);
    }

    @Test
    @WithMockUser(username = "test@user.com", authorities = {"ROLE_USER"})
    void requestAdoption_ShouldReturnSuccess() throws Exception {
        // Create another available pet
        Pet newPet = new Pet();
        newPet.setName("New Pet");
        newPet.setStatus(PetStatus.AVAILABLE);
        newPet = petRepository.save(newPet);

        mockMvc.perform(post("/api/adoptions/request/{petId}", newPet.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Adoption request sent successfully."));
    }

    @Test
    @WithMockUser(username = "test@user.com", authorities = {"ROLE_USER"})
    void requestAdoption_WhenPetAlreadyAdopted_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/adoptions/request/{petId}", adoptedPet.getId())
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Pet is already adopted."));
    }

    @Test
    @WithMockUser(username = "test@user.com", authorities = {"ROLE_USER"})
    void requestAdoption_WhenDuplicateRequest_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/adoptions/request/{petId}", availablePet.getId())
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You have already requested adoption for this pet."));
    }




    @Test
    @WithMockUser(username = "test@user.com", authorities = {"ROLE_USER"})
    void unadoptPet_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(put("/api/adoptions/unadopt/{petId}", adoptedPet.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Pet successfully un-adopted."));
    }

    @Test
    @WithMockUser(username = "test@user.com", authorities = {"ROLE_USER"})
    void unadoptPet_WhenNotAdopted_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/api/adoptions/unadopt/{petId}", availablePet.getId())
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Adoption record not found or pet is not adopted."));
    }

    @Test
    @WithMockUser(username = "test@user.com", authorities = {"ROLE_USER"})
    void unadoptPet_WhenPetNotInAdoptedStatus_ShouldReturnBadRequest() throws Exception {
        // Change pet status to something other than ADOPTED
        adoptedPet.setStatus(PetStatus.AVAILABLE);
        petRepository.save(adoptedPet);

        mockMvc.perform(put("/api/adoptions/unadopt/{petId}", adoptedPet.getId())
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Pet is not currently adopted."));
    }

    @Test
    @WithMockUser(username = "unauthorized@user.com", authorities = {"ROLE_USER"})
    void requestAdoption_WhenUnauthorizedUser_ShouldForbid() throws Exception {
        mockMvc.perform(post("/api/adoptions/request/{petId}", availablePet.getId())
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}