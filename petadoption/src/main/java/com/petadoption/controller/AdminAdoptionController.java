package com.petadoption.controller;

import com.petadoption.model.Adoption;
import com.petadoption.model.Pet;
import com.petadoption.model.PetStatus;
import com.petadoption.repository.AdoptionRepository;
import com.petadoption.repository.PetRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/adoptions")
public class AdminAdoptionController {

    private final AdoptionRepository adoptionRepository;
    private final PetRepository petRepository;

    public AdminAdoptionController(AdoptionRepository adoptionRepository, PetRepository petRepository) {
        this.adoptionRepository = adoptionRepository;
        this.petRepository = petRepository;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public List<Adoption> getAllAdoptions() {
        return adoptionRepository.findAll();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{adoptionId}/approve")
    public Adoption approveAdoption(@PathVariable Long adoptionId) {
        Adoption adoption = adoptionRepository.findById(adoptionId)
                .orElseThrow(() -> new RuntimeException("Adoption not found"));

        adoption.setStatus("APPROVED"); // Use string status instead of enum

        Pet pet = adoption.getPet();
        pet.setStatus(PetStatus.ADOPTED); // The pet's status still uses PetStatus enum (this is fine)

        petRepository.save(pet);
        return adoptionRepository.save(adoption);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{adoptionId}/reject")
    public Adoption rejectAdoption(@PathVariable Long adoptionId) {
        Adoption adoption = adoptionRepository.findById(adoptionId)
                .orElseThrow(() -> new RuntimeException("Adoption not found"));

        adoption.setStatus("REJECTED"); // Use string status instead of enum

        return adoptionRepository.save(adoption);
    }
}
