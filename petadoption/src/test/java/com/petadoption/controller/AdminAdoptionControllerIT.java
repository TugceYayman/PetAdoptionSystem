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
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminAdoptionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private AdoptionRepository adoptionRepository;

    private User regularUser;
    private Pet availablePet;
    private Pet adoptedPet;
    private Adoption pendingAdoption;
    private Adoption approvedAdoption;
    private Adoption rejectedAdoption;
    @BeforeEach
    void setUp() {

        // Create regular user
        regularUser = new User();
        regularUser.setEmail("user@petadoption.com");
        regularUser.setPassword("user123");
        regularUser.setName("Regular User");
        regularUser.setRole("ROLE_USER");
        regularUser = userRepository.save(regularUser);

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
        pendingAdoption = new Adoption(regularUser, availablePet, AdoptionStatus.PENDING);
        pendingAdoption = adoptionRepository.save(pendingAdoption);

        approvedAdoption = new Adoption(regularUser, adoptedPet, AdoptionStatus.APPROVED);
        approvedAdoption = adoptionRepository.save(approvedAdoption);

        rejectedAdoption = new Adoption(regularUser, availablePet, AdoptionStatus.REJECTED);
        rejectedAdoption = adoptionRepository.save(rejectedAdoption);
    }


    @Test
    @WithMockUser(username = "admin@petadoption.com", authorities = {"ADMIN"})
    void getPendingAdoptions_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/admin/adoptions/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status", everyItem(is("PENDING"))));

    }

    @Test
    @WithMockUser(username = "admin@petadoption.com", authorities = {"ADMIN"})
    void getPendingAdoptions_WhenEmpty_ShouldReturnNoContent() throws Exception {
        // Delete all pending adoptions
        adoptionRepository.deleteAll();

        mockMvc.perform(get("/api/admin/adoptions/pending"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@petadoption.com", authorities = {"ADMIN"})
    void approveAdoption_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(put("/api/admin/adoptions/approve/{requestId}", pendingAdoption.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Adoption approved successfully."));
    }

    @Test
    @WithMockUser(username = "admin@petadoption.com", authorities = {"ADMIN"})
    void approveAdoption_WhenPetNotAvailable_ShouldReturnBadRequest() throws Exception {
        // Create adoption for already adopted pet
        Adoption adoptionForAdoptedPet = new Adoption(regularUser, adoptedPet, AdoptionStatus.PENDING);
        adoptionForAdoptedPet = adoptionRepository.save(adoptionForAdoptedPet);

        mockMvc.perform(put("/api/admin/adoptions/approve/{requestId}", adoptionForAdoptedPet.getId())
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Pet is no longer available for adoption."));
    }

    @Test
    @WithMockUser(username = "admin@petadoption.com", authorities = {"ADMIN"})
    void approveAdoption_WhenRequestNotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/api/admin/adoptions/approve/99999")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Adoption request not found."));
    }

    @Test
    @WithMockUser(username = "admin@petadoption.com", authorities = {"ADMIN"})
    void rejectAdoption_ShouldReturnSuccess() throws Exception {
        // Create another pending adoption to reject
        Adoption newPendingAdoption = new Adoption(regularUser, availablePet, AdoptionStatus.PENDING);
        newPendingAdoption = adoptionRepository.save(newPendingAdoption);

        mockMvc.perform(put("/api/admin/adoptions/reject/{requestId}", newPendingAdoption.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Adoption request rejected."));
    }

    @Test
    @WithMockUser(username = "admin@petadoption.com", authorities = {"ADMIN"})
    void rejectAdoption_WhenAlreadyProcessed_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/admin/adoptions/reject/{requestId}", approvedAdoption.getId())
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("This request is already processed."));
    }

    @Test
    @WithMockUser(username = "admin@petadoption.com", authorities = {"ADMIN"})
    void rejectAdoption_WhenRequestNotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/api/admin/adoptions/reject/99999")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Adoption request not found."));
    }

    @Test
    @WithMockUser(username = "admin@petadoption.com", authorities = {"ADMIN"})
    void getAllAdoptions_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/admin/adoptions/adoption-list"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$[*].adoptionStatus", hasItems("APPROVED", "REJECTED")));

    }

    @Test
    @WithMockUser(username = "user@petadoption.com", authorities = {"ROLE_USER"})
    void getPendingAdoptions_WhenNotAdmin_ShouldForbid() throws Exception {
        mockMvc.perform(get("/api/admin/adoptions/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@petadoption.com", authorities = {"ADMIN"})
    void approveAdoption_ShouldUpdatePetStatus() throws Exception {
        mockMvc.perform(put("/api/admin/adoptions/approve/{requestId}", pendingAdoption.getId())
                .with(csrf()))
                .andExpect(status().isOk());

        Pet updatedPet = petRepository.findById(availablePet.getId()).orElseThrow();
        assertEquals(PetStatus.ADOPTED, updatedPet.getStatus());
    }

}