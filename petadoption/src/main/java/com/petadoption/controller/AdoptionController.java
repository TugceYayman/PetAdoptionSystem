package com.petadoption.controller;

import com.petadoption.model.Adoption;
import com.petadoption.model.Pet;
import com.petadoption.model.PetStatus;
import com.petadoption.model.User;
import com.petadoption.repository.AdoptionRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adoptions")
public class AdoptionController {

    private final AdoptionRepository adoptionRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;

    public AdoptionController(AdoptionRepository adoptionRepository, PetRepository petRepository, UserRepository userRepository) {
        this.adoptionRepository = adoptionRepository;
        this.petRepository = petRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/{petId}")
    public Adoption requestAdoption(@PathVariable Long petId, Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Pet pet = petRepository.findById(petId).orElseThrow(() -> new RuntimeException("Pet not found"));

        if (pet.getStatus() == PetStatus.ADOPTED) {
            throw new RuntimeException("Pet already adopted");
        }

        Adoption adoption = new Adoption();
        adoption.setUser(user);
        adoption.setPet(pet);
        adoption.setStatus("PENDING");   // Use String instead of enum

        return adoptionRepository.save(adoption);
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping
    public List<Adoption> getMyAdoptions(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return adoptionRepository.findByUser(user);
    }
}
